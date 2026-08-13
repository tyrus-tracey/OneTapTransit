package com.example.onetaptransit.staticdata

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query

@Dao
interface StaticDataDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertStop(stop: Stop): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun InsertStops(stops: List<Stop>) : List<Long>

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
}