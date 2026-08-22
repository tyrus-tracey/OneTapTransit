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
        SELECT Route.route_short_name, 
                Trip.trip_id, Trip.trip_headsign, 
                StopTime.arrival_time, StopTime.departure_time,
                StopTime.stop_sequence
        FROM Stop
        JOIN StopTime 
            ON StopTime.stop_id = Stop.stop_id
        JOIN Trip 
            ON Trip.trip_id = StopTime.trip_id
        JOIN Calendar 
            ON Calendar.service_id = Trip.service_id
        JOIN Route
            ON Route.route_id = Trip.route_id
        WHERE 
            Stop.stop_code = :stopCode AND
            CASE :weekday
                WHEN 'Mon' THEN Calendar.monday
                WHEN 'Tue' THEN Calendar.tuesday
                WHEN 'Wed' THEN Calendar.wednesday
                WHEN 'Thu' THEN Calendar.thursday
                WHEN 'Fri' THEN Calendar.friday
                WHEN 'Sat' THEN Calendar.saturday
                WHEN 'Sun' THEN Calendar.sunday
            END = 1 AND
            Calendar.start_date <= :date AND
            Calendar.end_date >= :date AND
            StopTime.arrival_time >= :time
        ORDER BY StopTime.arrival_time ASC
        LIMIT 1
    """)
    suspend fun testGetNextScheduledArrivalForStop(
        stopCode: Int, date: String, weekday: String, time: Long
    ): List<VehicleStopTime>
}