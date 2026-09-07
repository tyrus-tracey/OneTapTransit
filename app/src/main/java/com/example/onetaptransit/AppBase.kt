package com.example.onetaptransit

// Source - https://stackoverflow.com/a/63146319
// Posted by jsonV, modified by community. See post 'Timeline' for change history
// Retrieved 2026-08-12, License - CC BY-SA 4.0

import android.app.Application
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class AppBase : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration(): Configuration {
        Log.d("AppBase", "Creating WorkManager configuration with HiltWorkerFactory")

        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
}