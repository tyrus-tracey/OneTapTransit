package com.example.onetaptransit.ui.theme

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onetaptransit.BuildConfig
import com.google.transit.realtime.GtfsRealtime.FeedMessage
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
import kotlinx.io.readString


class TransitViewModel : ViewModel() {
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

            zip.entry(Path("agency.txt")) {
                Log.d("ZIP", readToSource().readString())
            }
            onProcessComplete()
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