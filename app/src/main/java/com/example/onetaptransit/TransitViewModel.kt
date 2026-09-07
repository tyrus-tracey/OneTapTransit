package com.example.onetaptransit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.example.onetaptransit.consts.REALTIME_PB_FILENAME
import com.example.onetaptransit.staticdata.StaticDataRepository
import com.example.onetaptransit.staticdata.VehicleStopTime
import com.example.onetaptransit.workers.RealtimeFeedFetcher
import com.example.onetaptransit.workers.StaticDataDBImporter
import com.example.onetaptransit.workers.StaticDataFetcher
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
        TransitState(nextArrival = VehicleStopTime(
            "","", ServiceTime(0), ServiceTime(0), "", 0
            )
        )
    )
    val transitState: StateFlow<TransitState> = _transitState.asStateFlow()

    fun updateRealtimeFeed(context: Context, onProcessComplete: () -> Unit) {
        val uniqueWorkName = "UPDATE_REALTIME_FEED"
        viewModelScope.launch {
            val realtimeFeedFetcher = OneTimeWorkRequestBuilder<RealtimeFeedFetcher>().build()
            WorkManager.getInstance(context).beginUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                realtimeFeedFetcher
            ).enqueue()

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

    fun updateStaticData(context: Context, onProcessComplete: () -> Unit) {
        val uniqueWorkName = "UPDATE_STATIC_DATA"
        viewModelScope.launch {
            val staticDataFetcher = OneTimeWorkRequestBuilder<StaticDataFetcher>().build()
            val staticDataDBImporter = OneTimeWorkRequestBuilder<StaticDataDBImporter>().build()

            WorkManager.getInstance(context).beginUniqueWork(
                uniqueWorkName,
                ExistingWorkPolicy.KEEP,
                listOf(staticDataFetcher, staticDataDBImporter)
            ).enqueue()

            //when (WorkManager.getInstance(context).getWorkInfoById(staticDataFetcher.id).get().state) {}
            if (WorkManager.getInstance(context).getWorkInfoById(staticDataFetcher.id).get().state.isFinished) {
                onProcessComplete()
            }
        }
    }

    fun queryNextArrival(
        onQueryResponse : (Result<VehicleStopTime>) -> Unit
    ) {
        viewModelScope.launch {
            val response = runCatching {
                val stopCode: Int = transitState.value.userEntryStopCode.toInt()
                val nextArrival = repo.getNextScheduledArrival(stopCode)
                nextArrival.first()
            } .onSuccess {
                setQuerySuccessState(true)
            } .onFailure {
                setQueryFailedState(true)
            }
            onQueryResponse(response)
        }
    }

    fun truncateTest(onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("TRACE", "- - - TRUNCATE START - - -")
            repo.truncateAllTables()
            Log.d("TRACE", "- - - TRUNCATE END - - -")
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