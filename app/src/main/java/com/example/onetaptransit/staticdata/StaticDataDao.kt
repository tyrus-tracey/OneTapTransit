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
        from Route
        inner join Trip
            on Route.route_id = Trip.route_id
        inner join StopTime
            on StopTime.trip_id = Trip.trip_id
        inner join Stop
            on Stop.stop_id = StopTime.stop_id
        WHERE 
            Stop.stop_code = :stopCode
            
        ORDER BY StopTime.arrival_time DESC
        LIMIT 1
    """)
    suspend fun testGetNextScheduledArrivalForStop(
        stopCode: Int
    ): List<StopTime>
}