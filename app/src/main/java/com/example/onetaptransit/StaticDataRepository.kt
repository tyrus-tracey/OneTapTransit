package com.example.onetaptransit

import javax.inject.Inject

class StaticDataRepository @Inject constructor(
    private val staticDataDao: StaticDataDao
) {
    suspend fun testQuery() = staticDataDao.getStopTest()
    suspend fun testInsert(stop: Stop) : Long {
        return staticDataDao.InsertStop(stop)
    }

    suspend fun testInsertMultiple(stops: List<Stop>) : List<Long> {
        return staticDataDao.InsertStops(stops)
    }

    suspend fun testCountStops() : Long {
        return staticDataDao.countStops()
    }
}