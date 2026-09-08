package com.example.onetaptransit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.onetaptransit.consts.KEY_STATIC_TABLE_NAME
import com.example.onetaptransit.consts.REALTIME_PB_FILENAME
import com.example.onetaptransit.staticdata.StaticDataRepository
import com.example.onetaptransit.staticdata.VehicleStopTime
import com.example.onetaptransit.workers.RealtimeFeedFetcher
import com.example.onetaptransit.workers.StaticDataFetcher
import com.example.onetaptransit.workers.StaticDataTableImporter
import com.example.onetaptransit.workers.StaticDataTableName
import com.example.onetaptransitprivate.ServiceTime
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val transitState: StateFlow<TransitState> = _transitState.asStateFlow()

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
            // TODO: getWorkInfos returns a list, from which I grab first(). seems a little weird,
            //  would be nice if there was a method that only returns a single workInfo.
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
     * Fetch static data from Translink API and import to DB.
     * Upon completion, call onProcessComplete().
     */
    fun updateStaticData(context: Context, onProcessComplete: () -> Unit) {
        val uniqueWorkName = "UPDATE_STATIC_DATA"
        viewModelScope.launch {
            val staticDataFetcher = OneTimeWorkRequestBuilder<StaticDataFetcher>().build()
            val workerList = mutableListOf<OneTimeWorkRequest>(staticDataFetcher)

            // TODO: Parallelize these importers
            for (table in StaticDataTableName.entries) {
                val tableImporter = OneTimeWorkRequestBuilder<StaticDataTableImporter>()
                    .setInputData(
                        workDataOf(
                            KEY_STATIC_TABLE_NAME to table.name
                        )
                    )
                    .build()
                workerList.addLast(tableImporter)
            }

            WorkManager.getInstance(context).beginUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                workerList
            ).enqueue()

            // Create listener for when work is complete.
            WorkManager.getInstance(context).getWorkInfosForUniqueWorkLiveData(uniqueWorkName).asFlow()
                .collect { workInfo ->
                    val workState = workInfo.first().state
                    if (workState == WorkInfo.State.SUCCEEDED) {
                        onProcessComplete()
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
}

data class TransitState(
    val realtimeFeed: FeedMessage = FeedMessage.getDefaultInstance(),
    val userEntryStopCode: String = "",
    val nextArrival: VehicleStopTime,
    val eQuerySuccess: Boolean = false,
    val eQueryFailed: Boolean = false
)