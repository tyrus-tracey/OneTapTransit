package com.example.onetaptransit.staticdata

import androidx.room3.ColumnInfo
import androidx.room3.ColumnTypeConverter
import androidx.room3.ColumnTypeConverters
import androidx.room3.Database
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import androidx.room3.RoomDatabase
import com.example.onetaptransitprivate.ServiceTime

@Database(
    entities = [Route::class, Trip::class, Stop::class, StopTime::class],
    version = 5,
    exportSchema = false
)
@ColumnTypeConverters(Converters::class)
abstract class StaticDataDB : RoomDatabase() {
    abstract fun staticDataDao(): StaticDataDao
}


@Entity
data class Route(
    @PrimaryKey @ColumnInfo("route_id") val routeID: String,
    @ColumnInfo("route_short_name") val routeShortName: String,
    @ColumnInfo("route_long_name") val routeLongName: String,
    @ColumnInfo("route_type") val routeType: String //TODO: create Enum converter
)

@Entity
data class Trip(
    @PrimaryKey @ColumnInfo("trip_id") val tripID: String,
    @ColumnInfo("route_id") val routeID: String,
    @ColumnInfo("trip_headsign") val tripHeadsign: String,
    @ColumnInfo("direction_id") val directionID: String //TODO: create Enum converter
)

@Entity(primaryKeys = ["trip_id", "stop_sequence"])
data class StopTime(
    @ColumnInfo("trip_id") val tripID: String,
    @ColumnInfo("stop_sequence") val stopSequence: Int,
    @ColumnInfo("arrival_time") val arrivalTime: ServiceTime,
    @ColumnInfo("departure_time") val departureTime: ServiceTime,
    @ColumnInfo("stop_id") val stopID : String,
)

@Entity
data class Stop(
    @PrimaryKey @ColumnInfo("stop_id") val stopID: String,
    @ColumnInfo("stop_code") val stopCode: String,
    @ColumnInfo("stop_name") val stopName: String,
    @ColumnInfo("zone_id") val zoneID: String
)

// A Route can correspond to many Trips
data class RouteWithTrips(
    @Embedded val route: Route,
    @Relation(
        parentColumns = ["route_id"],
        entityColumns = ["route_id"]
    )
   val trips: List<Trip>
)

// A Trip can correspond to many StopTimes
data class TripWithStopTimes(
    @Embedded val trip: Trip,
    @Relation(
        parentColumns = ["trip_id"],
        entityColumns = ["trip_id"]
    )
    val stopTimes: List<StopTime>
)

// A Stop can correspond to many StopTimes
data class StopWithStopTimes(
    @Embedded val stop: Stop,
    @Relation(
        parentColumns = ["stop_id"],    // from Stop
        entityColumns = ["stop_id"]     // from StopTime
    )
    val stopTimes: List<StopTime>
)

object Converters {
//    @ColumnTypeConverter
//    fun stringToServiceTime(value: String?) : ServiceTime? {
//        return value?.let { return ServiceTime(value) }
//    }
//
//    @ColumnTypeConverter
//    fun serviceTimeToString(serviceTime: ServiceTime?) : String? {
//        return serviceTime?.let { return serviceTime.toString() }
//    }

    @ColumnTypeConverter
    fun longToServiceTime(value: Long?) : ServiceTime? {
        return value?.let { return ServiceTime(value) }
    }

    @ColumnTypeConverter
    fun serviceTimeToLong(serviceTime: ServiceTime?) : Long? {
        return serviceTime?.let { return serviceTime.time }
    }

//    @ColumnTypeConverter
//    fun fromTimestamp(value: Long?): Date? {
//        return value?.let { Date(it) }
//    }
//
//    @ColumnTypeConverter
//    fun dateToTimestamp(date: Date?): Long? {
//        return date?.time
//    }
//
//    @ColumnTypeConverter
//    fun timeToEpoch(time: String?) : Long? {
//        val time_format = DateTimeFormatter.ofPattern("H:m:s a")
//        return time?.let {
//            LocalTime.parse(time, time_format).toEpochSecond(
//                LocalDate.now(),
//                ZoneOffset.of(ZoneId.systemDefault().id)
//            )
//        }
//    }
}

