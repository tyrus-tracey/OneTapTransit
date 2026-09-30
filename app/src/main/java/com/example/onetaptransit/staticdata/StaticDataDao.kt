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
    fun InsertCalendarDatesBlocking(calendarDates: List<CalendarDate>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertStopsBlocking(stops: List<Stop>) : List<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun InsertStopTimesBlocking(stop_times: List<StopTime>) : List<Long>

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
        DELETE from Calendar
    """
    )
    suspend fun truncateCalendar()

    @Query("""
        DELETE from CalendarDate
    """
    )
    suspend fun truncateCalendarDate()

    @Query("""
        DELETE from Stop
    """
    )
    suspend fun truncateStop()

    @Query("""
        DELETE from StopTime
    """)
    suspend fun truncateStopTime()

    /** Given a StopCode, date and time, return all of today's future scheduled arrivals. */
    @Transaction
    @Query("""
        WITH 
        ExceptionServiceAdded as (
            SELECT *
            FROM CalendarDate WHERE exception_type = 1
        ),
        ExceptionServiceCancelled as (
            SELECT *
            FROM CalendarDate WHERE exception_type = 2
        )
        
        SELECT Route.route_short_name, 
                Trip.trip_id, Trip.trip_headsign, 
                min(StopTime.arrival_time) as arrival_time, StopTime.departure_time,
                StopTime.stop_sequence
        FROM Stop
        JOIN StopTime 
            ON StopTime.stop_id = Stop.stop_id
        JOIN Trip 
            ON Trip.trip_id = StopTime.trip_id
        LEFT JOIN Calendar 
            ON Calendar.service_id = Trip.service_id
        JOIN Route
            ON Route.route_id = Trip.route_id
        WHERE (
            (
                Calendar.start_date <= :date AND
                Calendar.end_date >= :date AND
                CASE :weekday
                    WHEN 'Mon' THEN Calendar.monday
                    WHEN 'Tue' THEN Calendar.tuesday
                    WHEN 'Wed' THEN Calendar.wednesday
                    WHEN 'Thu' THEN Calendar.thursday
                    WHEN 'Fri' THEN Calendar.friday
                    WHEN 'Sat' THEN Calendar.saturday
                    WHEN 'Sun' THEN Calendar.sunday
                END = 1 AND
                NOT EXISTS (
                    SELECT e.* from ExceptionServiceCancelled e 
                    WHERE e.date = :date AND 
                    trip.service_id = e.service_id
                )
            )
            OR EXISTS (
                SELECT e.* from ExceptionServiceAdded e 
                WHERE e.date = :date AND 
                trip.service_id = e.service_id 
            )
        ) AND
            Stop.stop_code = :stopCode AND
            StopTime.arrival_time >= :time
        GROUP BY trip.trip_headsign
        ORDER BY StopTime.arrival_time ASC
    """)
    suspend fun getNextScheduledArrivalForStop(
        stopCode: Int, date: String, weekday: String, time: Long
    ): List<VehicleStopTime>
}