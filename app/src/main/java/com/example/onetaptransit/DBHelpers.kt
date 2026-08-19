package com.example.onetaptransitprivate

import com.example.onetaptransit.staticdata.Route
import com.example.onetaptransit.staticdata.Stop
import com.example.onetaptransit.staticdata.StopTime
import com.example.onetaptransit.staticdata.Trip
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        strtimeToEpoch(dataRow["arrival_time"]) ?: 0,
        strtimeToEpoch(dataRow["departure_time"]) ?: 0,
        dataRow["stop_id"] ?: "--",
    )
}

fun strtimeToEpoch(time: String?) : Long? {
    return time?.let {
        try {
            LocalTime.parse(time.trim(), TIME_FORMAT)
                .atDate(LocalDate.now()) //TODO: use trip date from calendar.txt
                .atZone(ZoneId.of(ZoneId.SHORT_IDS["PST"]))
                .toEpochSecond()
        }
        // Handle value roll-over beyond 23:59:59
        catch (e: java.time.format.DateTimeParseException) {
            val segments = time.split(":")
            var H = segments[0].toInt(); var M = segments[1].toInt(); var S = segments[2].toInt()
            var rollover_seconds: Long = 0

            if (H > 23) {
                rollover_seconds += (H - 23) * 3600
                H = 23
            }
            if (M > 59) {
                rollover_seconds += (M - 59) * 60
                M = 59
            }
            if (S > 59) {
                rollover_seconds += (S - 59)
                S = 59
            }
            LocalTime.parse("$H:$M:$S", TIME_FORMAT)
                .atDate(LocalDate.now()) //TODO: use trip date from calendar.txt
                .atZone(ZoneId.of(ZoneId.SHORT_IDS["PST"]))
                .toEpochSecond() + rollover_seconds
        }
    }
}

val TIME_FORMAT = DateTimeFormatter.ofPattern("H:m:s")