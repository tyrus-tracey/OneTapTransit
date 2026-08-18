package com.example.onetaptransit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onetaptransit.staticdata.StaticDataRepository
import com.example.onetaptransit.staticdata.Stop
import com.example.onetaptransit.staticdata.StopTime
import com.example.onetaptransitprivate.dataRowToStop
import com.example.onetaptransitprivate.dataRowToStopTime
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlinx.io.files.Path
import javax.inject.Inject


@HiltViewModel
class TransitViewModel @Inject constructor(
    private val repo: StaticDataRepository
) : ViewModel() {
    private val _transitState = MutableStateFlow(TransitState())
    val transitState: StateFlow<TransitState> = _transitState.asStateFlow()

    fun updateRealtimeFeed(onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            val newFeed = withContext(Dispatchers.IO) {
                APIRequestBuilder.tripUpdateRequest().openStream().use { inputStream ->
                    FeedMessage.parseFrom(inputStream)
                }
            }

            for (entity in newFeed.entityList) {
                if (entity.hasTripUpdate()) {
                    Log.d("TRIP_UPDATE", entity.tripUpdate.toString())
                }
            }

            _transitState.update {
                it.copy(realtimeFeed = newFeed)
            }
            onProcessComplete()
        }

    }

    fun updateStaticData(context: Context, onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            val zipFile = File(context.cacheDir, "staticData.zip")
            withContext(Dispatchers.IO) {
                APIRequestBuilder.gtfsStaticRequest().openStream().use { inputStream ->
                    zipFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val zipDir = zipFile.absolutePath
            val zip = Zip.open(Path(zipDir))

            withContext(Dispatchers.IO) {
                repo.importDataToDB<Stop>(
                    zip,
                    "stops.txt",
                    { stopRow -> dataRowToStop(stopRow) },
                    { stops -> repo.insertMultipleBlocking(stops) },
                    true
                )
                repo.importDataToDB<StopTime> (
                    zip,
                    "stop_times.txt",
                    { stopTimeRow -> dataRowToStopTime(stopTimeRow) },
                    { stopTimes -> repo.insertMultipleBlocking(stopTimes)},
                    true
                )
            }
            onProcessComplete()
        }
    }

    fun queryTest(onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("TRACE", "- - - QUERY START - - -")
            val test_response = repo.testStopWithStopsQuery()
            Log.d("RESPONSE", test_response.toString())
            Log.d("TRACE", "- - - QUERY END - - -")
        }
    }

    fun truncateTest(onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("TRACE", "- - - TRUNCATE START - - -")
            repo.truncateAllTables()
            Log.d("TRACE", "- - - TRUNCATE END - - -")
        }
    }

}

data class TransitState(
    var realtimeFeed: FeedMessage = FeedMessage.getDefaultInstance()
)