package com.example.onetaptransit.staticdata

import androidx.room3.ColumnInfo
import androidx.room3.Dao
import androidx.room3.Database
import androidx.room3.Entity
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.PrimaryKey
import androidx.room3.Query
import androidx.room3.RoomDatabase

@Database(
    entities = [Stop::class],
    version = 2,
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

//@Entity(primaryKeys = ["trip_id", "stop_sequence"])
//data class StopTimes(
//    @ColumnInfo("trip_id") val tripID: Int,
//    @ColumnInfo("stop_sequence") val stopSequence: Int,
//    @ColumnInfo("arrival_time") val arrivalTime: Date,
//    @ColumnInfo("departure_time") val departureTime: Date,
//    @ColumnInfo("stop_id") val stopID : Int,
//)