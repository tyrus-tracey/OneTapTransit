package com.example.onetaptransit

/**
 * Bundles stop information directly relevant to the user.
 * internalStopName: The stop name as written in stops.txt.
 * externalStopName: What is displayed in the UI, user-modifiable.
 * ignoreList: Trip headsigns the user does not want next arrival info for.
 */
data class TransitStop(
    val stopCode: String,
    val internalStopName: String,
    var externalStopName: String = internalStopName,
    val ignoreList: List<String> = emptyList()
)