package com.example.onetaptransit

data class TransitStop(
    val stopCode: Int,
    val internalStopName: String,
    var externalStopName: String,
    val ignoreList: List<String> = emptyList()
)