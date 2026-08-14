package com.example.onetaptransit

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onetaptransit.staticdata.StaticDataRepository
import com.example.onetaptransit.staticdata.Stop
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.reader.read
import com.jsoizo.kotlincsv.reader.withHeader
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
import java.net.URL
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
                translinkAPIRequest_TripUpdate().openStream().use { inputStream ->
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
            val response = withContext(Dispatchers.IO) {
                translinkStaticDataRequest().openStream().use { inputStream ->
                    zipFile.outputStream().use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }

            val zipDir = zipFile.absolutePath

            val zip = Zip.open(
                path = Path(path = zipDir),
                mode = Zip.Mode.Read,
                level = Zip.CompressionLevel.Default
            )

            var n_batches = 1
            var num_rows : Long = 0
            val buf_size = 500
            val buf_stops = ArrayList<Stop>(buf_size)

            withContext(Dispatchers.IO) {
                zip.entry(Path("stops.txt")) {
                    val reader = csvReader()
                    reader.read(source = readToSource()) { rows ->
                        Log.d("TRACE", "- - - BEGIN READ - - -")
                        rows.withHeader().forEach { row ->
                            buf_stops.add(
                                Stop(
                                    row["stop_id"] ?: "--",
                                    row["stop_code"] ?: "--",
                                    row["stop_name"] ?: "--",
                                    row["zone_id"] ?: "--"
                                )
                            )
                            if (buf_stops.size >= buf_size) {
                                val inserts = repo.testInsertMultipleBlocking(buf_stops)
                                val f = inserts.first()
                                val l = inserts.last()
                                num_rows += (l - f)
                                Log.d("BATCH INSERT", "Batch $n_batches: $num_rows rows.")
                                n_batches += 1
                                buf_stops.clear()
                                buf_stops.ensureCapacity(buf_size)
                            }
                        }
                        if (!buf_stops.isEmpty()) {
                            val inserts = repo.testInsertMultipleBlocking(buf_stops)
                            val f = inserts.first()
                            val l = inserts.last()
                            Log.d("BATCH INSERT", "Batch $n_batches: $num_rows rows.")
                            num_rows += (l - f)
                        }
                        buf_stops.clear()
                    }
                }

            }

            Log.d("INSERT", "Inserted $num_rows total rows across $n_batches batches.")
            onProcessComplete()
        }

    }

    fun queryTest(onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("TRACE", "- - - QUERY START - - -")
            val test_response = repo.testQuery()
            Log.d("QUERY RESPONSE", test_response.toString())
            Log.d("TRACE", "- - - QUERY END - - -")

            Log.d("QUERY RESPONSE", "There are ${repo.testCountStops()} stops.")
        }
    }

    fun truncateTest(onProcessComplete: () -> Unit) {
        viewModelScope.launch {
            Log.d("TRACE", "- - - TRUNCATE START - - -")
            repo.testTruncateStop()
            Log.d("TRACE", "- - - TRUNCATE START - - -")
        }
    }

    private fun translinkAPIRequest_TripUpdate() : URL {
        val key = BuildConfig.TRANSLINK_API_KEY
        return URL(" https://gtfsapi.translink.ca/v3/gtfsrealtime?apikey=$key")
    }

    private fun translinkAPIRequest_PositionUpdate() : URL {
        val key = BuildConfig.TRANSLINK_API_KEY
        return URL(" https://gtfsapi.translink.ca/v3/gtfsposition?apikey=$key")
    }

    private fun translinkAPIRequest_ServiceAlerts() : URL {
        val key = BuildConfig.TRANSLINK_API_KEY
        return URL(" https://gtfsapi.translink.ca/v3/gtfsalerts?apikey=$key")
    }
    private fun translinkStaticDataRequest() : URL {
        return URL("https://gtfs-static.translink.ca/gtfs/google_transit.zip")
    }
}

data class TransitState(
    var realtimeFeed: FeedMessage = FeedMessage.getDefaultInstance()
)