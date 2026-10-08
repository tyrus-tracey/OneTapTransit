package com.example.onetaptransit

data class TransitStop(
    val stopCode: String,
    val internalStopName: String,
    var externalStopName: String,
    val ignoreList: List<String> = emptyList()
)