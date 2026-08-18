package com.example.onetaptransitprivate

import com.example.onetaptransit.staticdata.Stop
import com.example.onetaptransit.staticdata.StopTime

fun dataRowToStop(dataRow: LinkedHashMap<String, String>) : Stop {
    return Stop(
        dataRow["stop_id"] ?: "--",
        dataRow["stop_code"] ?: "--",
        dataRow["stop_name"] ?: "--",
        dataRow["zone_id"] ?: "--"
    )
}

fun dataRowToStopTime(dataRow: LinkedHashMap<String, String>) : StopTime {
    return StopTime(
        dataRow["trip_id"] ?: "--",
        dataRow["stop_sequence"]?.toInt() ?: -1,
        dataRow["arrival_time"] ?: "--",
        dataRow["departure_time"] ?: "--",
        dataRow["stop_id"] ?: "--",
    )
}
