package com.example.onetaptransit.staticdata

import android.util.Log
import com.example.onetaptransitprivate.ServiceTime
import com.jsoizo.kotlincsv.csvReader
import com.jsoizo.kotlincsv.reader.read
import com.jsoizo.kotlincsv.reader.withHeader
import de.jonasbroeckmann.kzip.Zip
import kotlinx.io.files.Path
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale
import javax.inject.Inject

class StaticDataRepository @Inject constructor(
    private val staticDataDao: StaticDataDao
) {
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

    suspend fun truncateAllTables() {
        staticDataDao.truncateRoute()
        staticDataDao.truncateTrip()
        staticDataDao.truncateStop()
        staticDataDao.truncateStopTime()
    }

    /** For a given stop, return all of today's future scheduled stop times. */
    // TODO: Query currently does not check against CalendarDates.
    suspend fun getNextScheduledArrival(stopCode: Int) : List<VehicleStopTime> {
        val now = LocalDate.now()
        val date = now.format(DateTimeFormatter.BASIC_ISO_DATE).toString()
        val weekday = now.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CANADA)
        val time = ServiceTime(
                LocalTime.now().truncatedTo(ChronoUnit.SECONDS).format(DateTimeFormatter.ISO_LOCAL_TIME)
            )

        Log.d("INPUT", listOf<String>(stopCode.toString(), date, weekday.toString(), time.toString()).toString())
        return staticDataDao.testGetNextScheduledArrivalForStop(stopCode, date, weekday.toString(), time.time)
    }

    /** Import a given archived static data file to the Room DB. */
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

        // Returns # of rows inserted into DB.
        fun insertThenClearBuffer(buf: ArrayList<EntityType>) {
            val insertedRows = insertBatchToDB(buf)
            val batch_insert_count = insertedRows.last() - insertedRows.first() + 1
            n_batches += 1
            n_rows += batch_insert_count
            if (log_output) Log.d(dataFilename.uppercase(), "Batch $n_batches: $batch_insert_count rows.   $n_rows total.")

            buf.clear()
            buf.ensureCapacity(BUF_SIZE)
        }

        dataArchive.entry(Path(dataFilename)) {
            val buf = ArrayList<EntityType>(BUF_SIZE)
            val reader = csvReader()
            reader.read(source = readToSource()) { rows ->
                if (log_output) Log.d("importDataToDB", "- - - BEGIN READ: ${dataFilename.uppercase()} - - -")

                rows.withHeader().forEach { row ->
                    buf.add(dataRowToEntity(row))
                    if (buf.size >= BUF_SIZE) {
                        insertThenClearBuffer(buf)
                    }
                }

                if (!buf.isEmpty()) {
                    insertThenClearBuffer(buf)
                }
            }
            if (log_output) Log.d("importDataToDB","${dataFilename.uppercase()}: Inserted $n_rows total rows across $n_batches batches.")
        }
    }

}