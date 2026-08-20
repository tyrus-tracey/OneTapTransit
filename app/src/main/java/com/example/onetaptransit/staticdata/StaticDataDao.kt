package com.example.onetaptransit.staticdata

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction

@Dao
interface StaticDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertStop(stop: Stop): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertStops(stops: List<Stop>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertRoutesBlocking(routes: List<Route>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertTripsBlocking(trips: List<Trip>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertCalendarsBlocking(calendars: List<Calendar>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertStopsBlocking(stops: List<Stop>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertStopTimesBlocking(stop_times: List<StopTime>) : List<Long>

    @Query(
        """
            SELECT * from Stop
        """
    )
    suspend fun getStopTest() : List<Stop>

    @Query(
        """
            SELECT count(*) from Stop
        """
    )
    suspend fun countStops() : Long

    @Query("""
        DELETE from Route
    """
    )
    suspend fun truncateRoute()

    @Query("""
        DELETE from Trip
    """
    )
    suspend fun truncateTrip()

    @Query("""
        DELETE from Stop
    """
    )
    suspend fun truncateStop()

    @Query("""
        DELETE from StopTime
    """)
    suspend fun truncateStopTime()

    @Transaction
    @Query("""
        SELECT * from Stop
        WHERE Stop.stop_id = 1248
    """)
    suspend fun getStopsWithStopTimes(): List<StopWithStopTimes>

    @Transaction
    @Query("""
        SELECT StopTime.*
        FROM Stop
        JOIN StopTime 
            ON StopTime.stop_id = Stop.stop_id
        JOIN Trip 
            ON Trip.trip_id = StopTime.trip_id
        JOIN Calendar 
            ON Calendar.service_id = Trip.service_id
        WHERE 
            Stop.stop_code = :stopCode AND
            Calendar.thursday = 1 AND
            Calendar.start_date <= :date AND
            Calendar.end_date >= :date AND
            StopTime.arrival_time >= :time
        ORDER BY StopTime.arrival_time ASC
        LIMIT 1
    """)
    //TODO: figure out way to query for any weekday
    suspend fun testGetNextScheduledArrivalForStop(
        stopCode: Int, date: String, time: Long
    ): List<StopTime>
}