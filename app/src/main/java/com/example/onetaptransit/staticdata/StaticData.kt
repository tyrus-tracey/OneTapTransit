package com.example.onetaptransit.staticdata

import androidx.room3.ColumnInfo
import androidx.room3.Database
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import androidx.room3.RoomDatabase

@Database(
    entities = [Stop::class, StopTime::class],
    version = 3,
    exportSchema = false
)
//@ColumnTypeConverters(Converters::class)
abstract class StaticDataDB : RoomDatabase() {
    abstract fun staticDataDao(): StaticDataDao
}
@Entity
data class Stop(
    @PrimaryKey @ColumnInfo("stop_id") val stopID: String,
    @ColumnInfo("stop_code") val stopCode: String,
    @ColumnInfo("stop_name") val stopName: String,
    @ColumnInfo("zone_id") val zoneID: String
)

// A Stop can correspond to many StopTime records
// TODO: Create a Time converter for arrival/departure time
@Entity(primaryKeys = ["trip_id", "stop_sequence"])
data class StopTime(
    @ColumnInfo("trip_id") val tripID: String,
    @ColumnInfo("stop_sequence") val stopSequence: Int,
    @ColumnInfo("arrival_time") val arrivalTime: String,
    @ColumnInfo("departure_time") val departureTime: String,
    @ColumnInfo("stop_id") val stopID : String,
)

data class StopWithStopTimes(
    @Embedded val stop: Stop,
    @Relation(
        parentColumns = ["stop_id"],    // from Stop
        entityColumns = ["stop_id"]     // from StopTime
    )
    val stopTimes: List<StopTime>
)

//object Converters {
//    @ColumnTypeConverter
//    fun fromTimestamp(value: Long?): Date? {
//        return value?.let { Date(it) }
//    }
//
//    @ColumnTypeConverter
//    fun dateToTimestamp(date: Date?): Long? {
//        return date?.time
//    }
//}

