package com.example.onetaptransit

import java.net.URL

class APIRequestBuilder {
    companion object {
        fun gtfsStaticRequest() : URL {
            return URL("https://gtfs-static.translink.ca/gtfs/google_transit.zip")
        }

        fun gtfsRealtimeRequest() : URL {
            val key = BuildConfig.TRANSLINK_API_KEY
            return URL("https://gtfsapi.translink.ca/v3/gtfsrealtime?apikey=$key")
        }

        fun positionUpdateRequest() : URL {
            val key = BuildConfig.TRANSLINK_API_KEY
            return URL("https://gtfsapi.translink.ca/v3/gtfsposition?apikey=$key")
        }

        fun serviceAlertRequest() : URL {
            val key = BuildConfig.TRANSLINK_API_KEY
            return URL("https://gtfsapi.translink.ca/v3/gtfsalerts?apikey=$key")
        }
    }
}