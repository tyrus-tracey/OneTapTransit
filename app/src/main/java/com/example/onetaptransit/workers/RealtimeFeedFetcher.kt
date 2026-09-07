package com.example.onetaptransit.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onetaptransit.APIRequestBuilder
import com.google.transit.realtime.GtfsRealtime.FeedMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.io.File

class RealtimeFeedFetcher(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        val newFeed : FeedMessage =
            try {
                withContext(Dispatchers.IO) {
                    APIRequestBuilder.tripUpdateRequest().openStream().use { inputStream ->
                        FeedMessage.parseFrom(inputStream)
                    }
                }
            } catch (e: IOException) {
                Log.e(RealtimeFeedFetcher::class.simpleName, "Failed to retrieve realtime feed, retrying...", e)
                return Result.retry()
            } catch (e: Error) {
                Log.e(RealtimeFeedFetcher::class.simpleName, "Failed to retrieve realtime feed.", e)
                return Result.failure()
            }

        try {
            val dataFilePath = File(applicationContext.cacheDir, "realtimeData.pb")
            dataFilePath.setWritable(true)
            dataFilePath.writeBytes(newFeed.toByteArray())
            dataFilePath.setReadOnly()
        } catch (e: NullPointerException) {
            Log.e(RealtimeFeedFetcher::class.simpleName, "Failed to write protobuf to file.", e)
            return Result.failure()
        }

        return Result.success()
    }
}
