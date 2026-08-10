package com.example.onetaptransit.ui.theme

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.onetaptransit.BuildConfig
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


class TransitViewModel : ViewModel() {
    private val _transitState = MutableStateFlow(TransitState())
    val transitState: StateFlow<TransitState> = _transitState.asStateFlow()

    fun updateRealtimeFeed() {
        viewModelScope.launch {
            val newFeed = withContext(Dispatchers.IO) {
                translinkAPIRequestURl().openStream().use { inputStream ->
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
        }
    }

    private fun translinkAPIRequestURl() : URL {
        val key = BuildConfig.TRANSLINK_API_KEY
        return URL(" https://gtfsapi.translink.ca/v3/gtfsrealtime?apikey=$key")
    }
}

data class TransitState(
    var realtimeFeed: FeedMessage = FeedMessage.getDefaultInstance()
)