package com.example.onetaptransit.workers

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.onetaptransit.APIRequestBuilder
import com.example.onetaptransit.consts.STATIC_ZIP_FILENAME
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class StaticDataFetcher(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            return@withContext try{
                val dataFilePath = File(applicationContext.cacheDir, STATIC_ZIP_FILENAME)
                withContext(Dispatchers.IO) {
                    APIRequestBuilder.gtfsStaticRequest().openStream().use { inputStream ->
                        dataFilePath.outputStream().use { fileOutputStream ->
                            inputStream.copyTo(fileOutputStream)
                        }
                    }
                }
                Result.success()
            } catch (throwable: Throwable) {
                Log.e(StaticDataFetcher::class.simpleName, "Failed to fetch static data.", throwable)
                Result.failure()
            }
        }
    }
}

