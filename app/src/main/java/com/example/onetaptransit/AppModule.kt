package com.example.onetaptransit

// Source - https://stackoverflow.com/a/63146319
// Posted by jsonV, modified by community. See post 'Timeline' for change history
// Retrieved 2026-08-12, License - CC BY-SA 4.0

import android.content.Context
import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.example.onetaptransit.staticdata.StaticDataDB
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext

import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun providerDB(
        @ApplicationContext app: Context) =
        Room.databaseBuilder(app, StaticDataDB::class.java, "static_data_db")
            .setDriver(AndroidSQLiteDriver())
            .fallbackToDestructiveMigration()
            .build()

    @Singleton
    @Provides
    fun providerDao(db: StaticDataDB) = db.staticDataDao()
}