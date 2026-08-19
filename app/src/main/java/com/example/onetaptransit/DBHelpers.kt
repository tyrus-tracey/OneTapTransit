package com.example.onetaptransitprivate

import com.example.onetaptransit.staticdata.Route
import com.example.onetaptransit.staticdata.Stop
import com.example.onetaptransit.staticdata.StopTime
import com.example.onetaptransit.staticdata.Trip
import kotlin.collections.joinToString

fun dataRowToRoute(dataRow: LinkedHashMap<String, String>) : Route {
    return Route(
        dataRow["route_id"] ?: "--",
        dataRow["route_short_name"] ?: "--",
        dataRow["route_long_name"] ?: "--",
        dataRow["route_type"] ?: "--"
    )
}

fun dataRowToTrip(dataRow: LinkedHashMap<String, String>) : Trip {
    return Trip(
        dataRow["trip_id"] ?: "--",
        dataRow["route_id"]  ?: "--",
        dataRow["trip_headsign"] ?: "--",
        dataRow["direction_id"]  ?: "--"
    )
}

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
        ServiceTime(dataRow["arrival_time"] ?: "66:66:66"),
        ServiceTime(dataRow["departure_time"]?: "66:66:66"),
        dataRow["stop_id"] ?: "--",
    )
}
class ServiceTime(
    val time: Long
) {
    constructor(hours: Int, minutes: Int, seconds: Int) : this((hours * 3600).toLong() + (minutes * 60).toLong() + seconds)
    constructor(time: String) : this(time.trim().split(":").map { it.toInt() })
    private constructor(ints: List<Int>) : this(ints[0], ints[1], ints[2])

    fun hour(): Int { return (time / 3600).toInt() }
    fun minute(): Int { return ((time % 3600) / 60).toInt() }
    fun second(): Int { return (time % 60).toInt() }

    override fun toString(): String {
        return listOf(hour(), minute(), second()).joinToString(":")
    }
}