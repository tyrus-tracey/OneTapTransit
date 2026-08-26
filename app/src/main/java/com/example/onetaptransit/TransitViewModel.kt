package com.example.onetaptransit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onetaptransit.staticdata.Calendar
import com.example.onetaptransit.staticdata.Route
import com.example.onetaptransit.staticdata.StaticDataRepository
import com.example.onetaptransit.staticdata.Stop
import com.example.onetaptransit.staticdata.StopTime
import com.example.onetaptransit.staticdata.Trip
import com.example.onetaptransit.staticdata.VehicleStopTime
import com.example.onetaptransitprivate.ServiceTime
import com.example.onetaptransitprivate.dataRowToCalendar
import com.example.onetaptransitprivate.dataRowToRoute
import com.example.onetaptransitprivate.dataRowToStop
import com.example.onetaptransitprivate.dataRowToStopTime
import com.example.onetaptransitprivate.dataRowToTrip
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
import kotlinx.io.files.Path
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
                repo.importDataToDB<Route>(
                    zip,
                    "routes.txt",
                    { routeRow -> dataRowToRoute(routeRow) },
                    { routes -> repo.insertMultipleBlocking(routes)},
                    true
                )
                repo.importDataToDB<Trip>(
                zip,
                    "trips.txt",
                    { tripRow -> dataRowToTrip(tripRow) },
                    { trips -> repo.insertMultipleBlocking(trips) },
                    true
                )
                repo.importDataToDB<Calendar>(
                    zip,
                    "calendar.txt",
                    { calendarRow -> dataRowToCalendar(calendarRow) },
                    { calendars -> repo.insertMultipleBlocking(calendars) },
                    true
                )
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