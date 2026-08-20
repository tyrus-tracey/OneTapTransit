package com.example.onetaptransit.staticdata

import android.util.Log
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.reader.read
import com.jsoizo.kotlincsv.reader.withHeader
import de.jonasbroeckmann.kzip.Zip
import javax.inject.Inject
import kotlinx.io.files.Path
import kotlin.sequences.forEach

class StaticDataRepository @Inject constructor(
    private val staticDataDB: StaticDataDB,
    private val staticDataDao: StaticDataDao
) {
    suspend fun testQuery() = staticDataDao.getStopTest()

    @JvmName("insertMultipleRoutesBlocking")
    fun insertMultipleBlocking(routes: List<Route>) : List<Long> {
        return staticDataDao.InsertRoutesBlocking(routes)
    }

    @JvmName("insertMultipleTripsBlocking")
    fun insertMultipleBlocking(trips: List<Trip>) : List<Long> {
        return staticDataDao.InsertTripsBlocking(trips)
    }

    @JvmName("insertMultipleStopsBlocking")
    fun insertMultipleBlocking(stops: List<Stop>) : List<Long> {
        return staticDataDao.InsertStopsBlocking(stops)
    }
    @JvmName("insertMultipleStopTimesBlocking")
    fun insertMultipleBlocking(stop_times: List<StopTime>) : List<Long> {
        return staticDataDao.InsertStopTimesBlocking(stop_times)
    }
    @JvmName("insertMultipleCalendarsBlocking")
    fun insertMultipleBlocking(calendars: List<Calendar>) : List<Long> {
        return staticDataDao.InsertCalendarsBlocking(calendars)
    }

    suspend fun countAllStops() : Long {
        return staticDataDao.countStops()
    }
    suspend fun truncateAllTables() {
        staticDataDao.truncateRoute()
        staticDataDao.truncateTrip()
        staticDataDao.truncateStop()
        staticDataDao.truncateStopTime()
    }

    suspend fun testStopWithStopsQuery() = staticDataDao.getStopsWithStopTimes()

    suspend fun testGetNextScheduledArrivalFor51238() = staticDataDao.testGetNextScheduledArrivalForStop(51238)

    suspend fun <EntityType> importDataToDB(
        dataArchive: Zip,
        dataFilename: String,
        dataRowToEntity: (LinkedHashMap<String, String>) -> EntityType,
        insertBatchToDB: (List<EntityType>) -> List<Long>,
        log_output: Boolean = false
    ) {
        val BUF_SIZE = 5000
        var n_batches = 0
        var n_rows : Long = 0

        fun insertAndClearBuffer(buf: ArrayList<EntityType>) : Long {
            val inserts = insertBatchToDB(buf)
            val batch_insert_count = inserts.last() - inserts.first() + 1

            if (log_output) Log.d("BATCH INSERT", "Batch $n_batches: $batch_insert_count rows.")

            buf.clear()
            buf.ensureCapacity(BUF_SIZE)
            return batch_insert_count
        }

        dataArchive.entry(Path(dataFilename)) {
            val buf = ArrayList<EntityType>(BUF_SIZE)
            val reader = csvReader()
            reader.read(source = readToSource()) { rows ->
                if (log_output) Log.d("TRACE", "- - - BEGIN READ: ${dataFilename.uppercase()} - - -")

                rows.withHeader().forEach { row ->
                    buf.add(dataRowToEntity(row))
                    if (buf.size >= BUF_SIZE) {
                        n_batches += 1
                        n_rows += insertAndClearBuffer(buf)
                    }
                }
                if (!buf.isEmpty()) {
                    n_batches += 1
                    n_rows += insertAndClearBuffer(buf)
                }
                buf.clear()
            }
            if (log_output) Log.d("INSERT","${dataFilename.uppercase()}: Inserted $n_rows total rows across $n_batches batches.")
        }
    }

}