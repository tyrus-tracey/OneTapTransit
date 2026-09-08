package com.example.onetaptransit.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onetaptransit.consts.KEY_STATIC_TABLE_NAME
import com.example.onetaptransit.consts.STATIC_ZIP_FILENAME
import com.example.onetaptransit.staticdata.Calendar
import com.example.onetaptransit.staticdata.Route
import com.example.onetaptransit.staticdata.StaticDataRepository
import com.example.onetaptransit.staticdata.Stop
import com.example.onetaptransit.staticdata.StopTime
import com.example.onetaptransit.staticdata.Trip
import com.example.onetaptransitprivate.dataRowToCalendar
import com.example.onetaptransitprivate.dataRowToRoute
import com.example.onetaptransitprivate.dataRowToStop
import com.example.onetaptransitprivate.dataRowToStopTime
import com.example.onetaptransitprivate.dataRowToTrip
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import de.jonasbroeckmann.kzip.Zip
import de.jonasbroeckmann.kzip.open
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.files.Path
import java.io.File

/**
 * Imports downloaded GTFS static data files into Room DB.
 * Input workData: KEY_STATIC_TABLE_NAME maps to a StaticDataTableName value (as a string) which is
 *      used to construct the table filename and select the correct data translation/insertion functions.
 * Returns Result.success() if imports is successful.
 * Returns Result.failure() upon any throwable.
 */
@HiltWorker
class StaticDataTableImporter @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: StaticDataRepository
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val zipFile = File(applicationContext.cacheDir, STATIC_ZIP_FILENAME)
        val zipDir = zipFile.absolutePath
        val zip = Zip.open(Path(zipDir))

        val table = StaticDataTableName.fromString(inputData.getString(KEY_STATIC_TABLE_NAME) ?: "")

        return withContext(Dispatchers.IO) {
            return@withContext try {
                when (table) {
                    StaticDataTableName.ROUTES -> {
                        repo.importDataToDB<Route>(
                            zip,
                            "routes.txt",
                            { routeRow -> dataRowToRoute(routeRow) },
                            { routes -> repo.insertMultipleBlocking(routes)},
                            true
                        )
                    }
                    StaticDataTableName.TRIPS -> {
                        repo.importDataToDB<Trip>(
                            zip,
                            "trips.txt",
                            { tripRow -> dataRowToTrip(tripRow) },
                            { trips -> repo.insertMultipleBlocking(trips) },
                            true
                        )
                    }
                    StaticDataTableName.CALENDAR -> {
                        repo.importDataToDB<Calendar>(
                            zip,
                            "calendar.txt",
                            { calendarRow -> dataRowToCalendar(calendarRow) },
                            { calendars -> repo.insertMultipleBlocking(calendars) },
                            true
                        )
                    }
                    StaticDataTableName.CALENDAR_DATES -> { throw Error("Calendar date importer: To be implemented") }
                    StaticDataTableName.STOPS -> {
                        repo.importDataToDB<Stop>(
                            zip,
                            "stops.txt",
                            { stopRow -> dataRowToStop(stopRow) },
                            { stops -> repo.insertMultipleBlocking(stops) },
                            true
                        )
                    }
                    StaticDataTableName.STOP_TIMES -> {
                        repo.importDataToDB<StopTime> (
                            zip,
                            "stop_times.txt",
                            { stopTimeRow -> dataRowToStopTime(stopTimeRow) },
                            { stopTimes -> repo.insertMultipleBlocking(stopTimes)},
                            true
                        )
                    }
                }
                Result.success()
            } catch (throwable: Throwable) {
                Log.e(StaticDataTableImporter::class.simpleName, "Failed to import static data to database.", throwable)
                Result.failure()
            }
        }
    }
}

enum class StaticDataTableName {
    ROUTES, TRIPS, CALENDAR, CALENDAR_DATES, STOPS, STOP_TIMES;

    companion object {
        fun fromString(s: String) : StaticDataTableName {
            when (s) {
                "ROUTES" -> return ROUTES
                "TRIPS" -> return TRIPS
                "CALENDAR" -> return CALENDAR
                "CALENDAR_DATES" -> return CALENDAR_DATES
                "STOPS" -> return STOPS
                "STOP_TIMES" -> return STOP_TIMES
                else -> throw IllegalArgumentException("Invalid input string value for converting to StaticDataTableName enum")
            }
        }
    }
}