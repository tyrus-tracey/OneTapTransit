package com.example.onetaptransit.workers

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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

@HiltWorker
class StaticDataDBImporter @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: StaticDataRepository
) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val zipFile = File(applicationContext.cacheDir, STATIC_ZIP_FILENAME)
        val zipDir = zipFile.absolutePath
        val zip = Zip.open(Path(zipDir))

        return withContext(Dispatchers.IO) {
            return@withContext try {
                repo.importDataToDB<Route>(
                    zip,
                    "routes.txt",
                    { routeRow -> dataRowToRoute(routeRow) },
                    { routes -> repo.insertMultipleBlocking(routes)},
                    true
                )
                repo.importDataToDB<Trip>(
                    zip,
                    "trips.txt",
                    { tripRow -> dataRowToTrip(tripRow) },
                    { trips -> repo.insertMultipleBlocking(trips) },
                    true
                )
                repo.importDataToDB<Calendar>(
                    zip,
                    "calendar.txt",
                    { calendarRow -> dataRowToCalendar(calendarRow) },
                    { calendars -> repo.insertMultipleBlocking(calendars) },
                    true
                )
                repo.importDataToDB<Stop>(
                    zip,
                    "stops.txt",
                    { stopRow -> dataRowToStop(stopRow) },
                    { stops -> repo.insertMultipleBlocking(stops) },
                    true
                )
                repo.importDataToDB<StopTime> (
                    zip,
                    "stop_times.txt",
                    { stopTimeRow -> dataRowToStopTime(stopTimeRow) },
                    { stopTimes -> repo.insertMultipleBlocking(stopTimes)},
                    true
                )
                Result.success()
            } catch (throwable: Throwable) {
                Log.e(StaticDataDBImporter::class.simpleName, "Failed to import static data to database.", throwable)
                Result.failure()
            }
        }
    }
}