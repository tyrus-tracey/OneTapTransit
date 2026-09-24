package com.example.onetaptransit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.onetaptransit.consts.KEY_STATIC_TABLE_NAME
import com.example.onetaptransit.consts.REALTIME_PB_FILENAME
import com.example.onetaptransit.consts.STATIC_DATA_IMPORT_UNIQUE_WORK_NAME
import com.example.onetaptransit.consts.STATIC_DATA_TABLE_IMPORTER_TAG
import com.example.onetaptransit.consts.WORKER_PROGRESS
import com.example.onetaptransit.notifications.launchGTFSStaticImportNotification
import com.example.onetaptransit.staticdata.StaticDataRepository
import com.example.onetaptransit.staticdata.VehicleStopTime
import com.example.onetaptransit.workers.RealtimeFeedFetcher
import com.example.onetaptransit.workers.StaticDataFetcher
import com.example.onetaptransit.workers.StaticDataTableImporter
import com.example.onetaptransit.workers.StaticDataTableName
import com.example.onetaptransitprivate.ServiceTime
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@HiltViewModel
class TransitViewModel @Inject constructor(
    private val repo: StaticDataRepository
) : ViewModel() {
    private val _transitState = MutableStateFlow(
        //TODO: TransitState requires an initial value for nextArrival, review if
        //  this default value makes sense or whether it should be nullable instead.
        TransitState(nextArrival = VehicleStopTime(
            "","", ServiceTime(0), ServiceTime(0), "", 0
            )
        )
    )
    private val _gtfsStaticDataImportState = MutableStateFlow(GTFSStaticDataImportProgressState())
    val transitState = _transitState.asStateFlow()
    val gtfsStaticDataImportState = _gtfsStaticDataImportState.asStateFlow()

    /**
     * Fetch feed from Translink API.
     * Upon completion, dump feed to Log.
     */
    fun updateRealtimeFeed(context: Context, onProcessComplete: () -> Unit) {
        val uniqueWorkName = "UPDATE_REALTIME_FEED"
        viewModelScope.launch {
            val realtimeFeedFetcher = OneTimeWorkRequestBuilder<RealtimeFeedFetcher>().build()
            WorkManager.getInstance(context).beginUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                realtimeFeedFetcher
            ).enqueue()


            // Create listener for when work is complete.
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(uniqueWorkName).asFlow()
                .collect { workInfo ->
                    val workState = workInfo.first().state
                    if (workState == WorkInfo.State.SUCCEEDED) {
                        val realtimeFeedFile = File(context.cacheDir, REALTIME_PB_FILENAME)
                        val updatedFeed = FeedMessage.parseFrom(realtimeFeedFile.readBytes())

                        _transitState.update {
                            it.copy(realtimeFeed = updatedFeed)
                        }

                        val entityList = transitState.value.realtimeFeed.entityList
                        for (entity in entityList) {
                            if (entity.hasTripUpdate()) {
                                Log.d("REALTIME ENTITY", entity.getTripUpdate().toString())
                            }
                        }
                        onProcessComplete()
                    }
                }
        }
    }

    /**
     * Fetch static data from Translink API and import tables to DB.
     * Upon completion, call onProcessComplete().
     */
    // Store scoped job so that process can be canceled, as calling WorkManager.cancelWork
    //  won't stop the process the listeners are set up in.
    private var importProgressObserver: Job? = null
    fun updateStaticData(context: Context, onProcessComplete: () -> Unit) {
        importProgressObserver?.cancel()
        importProgressObserver = viewModelScope.launch {
            updateIsLoading(true)
            val dataFetcher = OneTimeWorkRequestBuilder<StaticDataFetcher>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            val tableImporters = mutableListOf<OneTimeWorkRequest>()

            // Create list of import workers
            // Using setExpedited() seems to persist better in background
            // Importer tag + table name tags used to later identify these workers
            for (table in StaticDataTableName.entries) {
                val tableImporter = OneTimeWorkRequestBuilder<StaticDataTableImporter>()
                    .setInputData(
                        workDataOf(
                            KEY_STATIC_TABLE_NAME to table.name
                        )
                    )
                    .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                    .addTag(STATIC_DATA_TABLE_IMPORTER_TAG)
                    .addTag(table.name)
                    .build()
                tableImporters.addLast(tableImporter)
            }

            // Start work chain of fetcher, then importers
            WorkManager.getInstance(context).beginUniqueWork(
                STATIC_DATA_IMPORT_UNIQUE_WORK_NAME,
                ExistingWorkPolicy.KEEP,
                dataFetcher
            )
                .then(tableImporters)
                .enqueue()

            val job_notification = launch {
                gtfsStaticDataImportState.collect { state ->
                    launchGTFSStaticImportNotification(
                        999,
                        state.progRoutes,
                        state.progTrips,
                        state.progCalendar,
                        state.progCalendarDates,
                        state.progStops,
                        state.progStopTimes,
                        context
                    )
                }
            }

            // Create listeners for work progress and completion state changes.
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(STATIC_DATA_IMPORT_UNIQUE_WORK_NAME)
                .asFlow()
                .collect { workInfos ->
                    // For each importer, listen to progress changes and update ViewModel
                    val tableImporterInfos = workInfos.filter { it.tags.contains(STATIC_DATA_TABLE_IMPORTER_TAG) }

                    for (tableImporterInfo in tableImporterInfos) {
                        val tableTag = tableImporterInfo.tags.first { tag ->
                            StaticDataTableName.entries.any { it.name == tag }
                        }

                        val table = StaticDataTableName.fromString(tableTag)
                        val importProgress = tableImporterInfo.progress.getInt(WORKER_PROGRESS, 0)

                        if (tableImporterInfo.state == WorkInfo.State.RUNNING) {
                            updateImportProgress(table, importProgress, true)
                        }
                    }

                    if (workInfos.all { it.state == WorkInfo.State.SUCCEEDED } ) {
                        updateIsLoading(false)
                        onProcessComplete()
                        job_notification.cancel()
                    }
                }
        }
    }

    /**
     * Read user's inputted StopCode from ViewModel and query for the next scheduled bus arrival.
     * Currently returns only the soonest vehicle out of the list of future stop times.
     */
    fun queryNextArrival(
        onQueryResponse : (Result<VehicleStopTime>) -> Unit
    ) {
        viewModelScope.launch {
            val response = runCatching {
                val stopCode: Int = transitState.value.userEntryStopCode.toInt()
                val nextArrival = repo.getNextScheduledArrival(stopCode)
                nextArrival.first() // soonest vehicle
            } .onSuccess {
                setQuerySuccessState(true)
            } .onFailure {
                setQueryFailedState(true)
            }
            onQueryResponse(response)
        }
    }

    fun truncateAllTables(onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("TRACE", "- - - TRUNCATE START - - -")
            repo.truncateAllTables()
            Log.d("TRACE", "- - - TRUNCATE END - - -")
            onProcessComplete()
        }
    }

    fun cancelStaticDataImport(context: Context) {
        updateIsLoading(false)
        WorkManager.getInstance(context).cancelUniqueWork(STATIC_DATA_IMPORT_UNIQUE_WORK_NAME)
        importProgressObserver?.cancel()
        WorkManager.getInstance(context).pruneWork()
    }

    fun updateUserEntryStopCode(newStopCode: String) {
        _transitState.update { it.copy(userEntryStopCode = newStopCode) }
    }

    fun updateNextArrival(newArrival: VehicleStopTime) {
        _transitState.update { it.copy(nextArrival = newArrival) }
    }

    fun setQuerySuccessState(newState: Boolean) {
        _transitState.update { it.copy(eQuerySuccess = newState) }
    }

    fun setQueryFailedState(newState: Boolean) {
        _transitState.update { it.copy(eQueryFailed = newState) }
    }

    fun updateIsLoading(newState: Boolean) {
        _gtfsStaticDataImportState.update { it.copy(isLoading = newState) }
    }

    fun updateImportProgress(table: StaticDataTableName, progress: Int, show_debug: Boolean = false) {
        if (!(progress in 0..100)) {
            throw IllegalArgumentException("Invalid GTFS Static Data import progress value: $progress")
        }

        fun log_progress(tableName: String, progress: Int) {
            Log.d(tableName, "Imported: " + progress + "%")
        }

        when (table) {
            StaticDataTableName.ROUTES -> {
                _gtfsStaticDataImportState.update { it.copy(progRoutes = progress) }
                if (show_debug) log_progress(table.name, gtfsStaticDataImportState.value.progRoutes)
            }
            StaticDataTableName.TRIPS -> {
                _gtfsStaticDataImportState.update { it.copy(progTrips = progress) }
                if (show_debug) log_progress(table.name, gtfsStaticDataImportState.value.progTrips)
            }
            StaticDataTableName.CALENDAR -> {
                _gtfsStaticDataImportState.update { it.copy(progCalendar = progress) }
                if (show_debug) log_progress(table.name, gtfsStaticDataImportState.value.progCalendar)
            }
            StaticDataTableName.CALENDAR_DATES -> {
                if (show_debug) Log.d("TransitViewModel", "updateImportProgress(): Handling for CALENDAR_DATES to be implemented.")
            }
            StaticDataTableName.STOPS -> {
                _gtfsStaticDataImportState.update { it.copy(progStops = progress) }
                if (show_debug) log_progress(table.name, gtfsStaticDataImportState.value.progStops)
            }
            StaticDataTableName.STOP_TIMES -> {
                _gtfsStaticDataImportState.update { it.copy(progStopTimes = progress) }
                if (show_debug) log_progress(table.name, gtfsStaticDataImportState.value.progStopTimes)
            }
        }
    }
}

data class TransitState(
    val realtimeFeed: FeedMessage = FeedMessage.getDefaultInstance(),
    val userEntryStopCode: String = "",
    val nextArrival: VehicleStopTime,
    val eQuerySuccess: Boolean = false,
    val eQueryFailed: Boolean = false
)

data class GTFSStaticDataImportProgressState(
    val isLoading: Boolean = false,
    val progRoutes: Int = 0,
    val progTrips: Int = 0,
    val progCalendar: Int = 0,
    val progCalendarDates: Int = 0,
    val progStops: Int = 0,
    val progStopTimes: Int = 0
)