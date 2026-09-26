package com.example.onetaptransitprivate

import com.example.onetaptransit.staticdata.Calendar
import com.example.onetaptransit.staticdata.Route
import com.example.onetaptransit.staticdata.Stop
import com.example.onetaptransit.staticdata.StopTime
import com.example.onetaptransit.staticdata.Trip
import java.time.LocalDate
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
        dataRow["service_id"] ?: "--",
        dataRow["route_id"]  ?: "--",
        dataRow["trip_headsign"] ?: "--",
        dataRow["direction_id"]  ?: "--"
    )
}

fun dataRowToCalendar(dataRow: LinkedHashMap<String, String>) : Calendar {
    return Calendar(
        dataRow["service_id"] ?: "--",
        ServiceWeekday(dataRow["monday"] ?: "TERRIBLE ERROR"), //TODO: find suitable alternative if dataRow[] is null
        ServiceWeekday(dataRow["tuesday"] ?: "TERRIBLE ERROR"),
        ServiceWeekday(dataRow["wednesday"] ?: "TERRIBLE ERROR"),
        ServiceWeekday(dataRow["thursday"] ?: "TERRIBLE ERROR"),
        ServiceWeekday(dataRow["friday"] ?: "TERRIBLE ERROR"),
        ServiceWeekday(dataRow["saturday"] ?: "TERRIBLE ERROR"),
        ServiceWeekday(dataRow["sunday"] ?: "TERRIBLE ERROR"),
        LocalDate.parse(dataRow["start_date"] ?: "TERRIBLE ERROR", DateTimeFormatter.BASIC_ISO_DATE),
        LocalDate.parse(dataRow["end_date"] ?: "TERRIBLE ERROR", DateTimeFormatter.BASIC_ISO_DATE)
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
        ServiceTime(dataRow["arrival_time"] ?: "TERRIBLE ERROR"),   //TODO: find suitable alternative if dataRow[] is null
        ServiceTime(dataRow["departure_time"]?: "TERRIBLE ERROR"),
        dataRow["stop_id"] ?: "--",
    )
}

/** Converts H:M:S format into # of seconds elapsed since start of day, including when exceeding 23:59:59. */
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

/**
 * Describes service availability for a given weekday, according to Calendar's defined date range.
 * IN_SERVICE: Service is available for all instances in the date range.
 * NOT_IN_SERVICE: Service is not available for instances in the date range.
 *
 * MUST check against CalendarDates for service exceptions.
 */
class ServiceWeekday(operation: Int) {
    val weeklyOperation: WeeklyOperation

    constructor(operation: String) : this(operation.toInt())
    init {
        if (operation == 1) {
            weeklyOperation = WeeklyOperation.IN_SERVICE
        } else {
            weeklyOperation = WeeklyOperation.NOT_IN_SERVICE
        }
    }

    fun isInServiceEveryWeek() : Boolean {
        return weeklyOperation == WeeklyOperation.IN_SERVICE
    }

    enum class WeeklyOperation {
        NOT_IN_SERVICE,
        IN_SERVICE
    }
}

enum class StaticTable {
    ROUTES, TRIPS, CALENDAR, CALENDAR_DATES, STOPS, STOP_TIMES
}