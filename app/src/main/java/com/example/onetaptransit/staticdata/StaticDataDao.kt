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
        SELECT * from stop
        WHERE stop.stop_id = 1248
    """)
    suspend fun getStopsWithStopTimes(): List<StopWithStopTimes>
}