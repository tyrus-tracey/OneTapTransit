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
import com.example.onetaptransitprivate.ServiceWeekday
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Database(
    entities = [Route::class, Trip::class, Calendar::class, Stop::class, StopTime::class],
    version = 7,
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
    @ColumnInfo("service_id") val serviceID: String,
    @ColumnInfo("route_id") val routeID: String,
    @ColumnInfo("trip_headsign") val tripHeadsign: String,
    @ColumnInfo("direction_id") val directionID: String //TODO: create Enum converter
)

@Entity
data class Calendar(
    @PrimaryKey @ColumnInfo("service_id") val serviceID: String,
    val monday: ServiceWeekday,
    val tuesday: ServiceWeekday,
    val wednesday: ServiceWeekday,
    val thursday: ServiceWeekday,
    val friday: ServiceWeekday,
    val saturday: ServiceWeekday,
    val sunday: ServiceWeekday,
    @ColumnInfo("start_date") val startDate: LocalDate,
    @ColumnInfo("end_date") val endDate: LocalDate
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
    @ColumnTypeConverter
    fun longToServiceTime(value: Long?) : ServiceTime? {
        return value?.let { return ServiceTime(value) }
    }

    @ColumnTypeConverter
    fun serviceTimeToLong(serviceTime: ServiceTime?) : Long? {
        return serviceTime?.let { return serviceTime.time }
    }

    @ColumnTypeConverter
    fun intToServiceWeekday(value: Int?) : ServiceWeekday? {
        return value?.let {
            ServiceWeekday(value)
        }
    }

    @ColumnTypeConverter
    fun serviceWeekdayToInt(serviceWeekday: ServiceWeekday?) : Int? {
        return serviceWeekday?.let {
            if (serviceWeekday.isInServiceEveryWeek()) {
                return 1
            } else {
                return 0
            }
        }
    }

    @ColumnTypeConverter
    fun stringToLocalDate(value: String?): LocalDate? {
        return value?.let {
            LocalDate.parse(value, DateTimeFormatter.BASIC_ISO_DATE)
        }
    }

    @ColumnTypeConverter
    fun localDateToString(date: LocalDate?) : String? {
        return date?.let {
            date.format(DateTimeFormatter.BASIC_ISO_DATE)
        }
    }
}

