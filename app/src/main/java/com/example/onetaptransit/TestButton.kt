package com.example.onetaptransit

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URL


suspend fun queryAPI() : String {
    val key = BuildConfig.TRANSLINK_API_KEY
    val query_url = URL(" https://gtfsapi.translink.ca/v3/gtfsrealtime?apikey=$key")

    Log.d("TRACE", "- - - - TRACE - - - -")

    return withContext(Dispatchers.IO) {
        query_url.openStream().use { inputStream ->
            val feed = FeedMessage.parseFrom(inputStream)
            for (entity in feed.entityList) {
                if (entity.hasTripUpdate()) {
                    Log.d("TRIP_UPDATE", entity.tripUpdate.toString())
                }
            }
        }
        "done"
    }
}

@Composable
fun TestButton() {
    var response by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Button(onClick = {
            scope.launch {
                response = queryAPI()
            }
        })
        {
            Text("Try me")
        }

        response?.let {
            Text(it)
        }
    }
}
