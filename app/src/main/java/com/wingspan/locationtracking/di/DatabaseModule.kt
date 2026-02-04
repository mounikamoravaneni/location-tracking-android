package com.wingspan.locationtracking.di

import android.content.Context
import androidx.room.Room
import com.wingspan.locationtracking.data.data.local.AppDatabase
import com.wingspan.locationtracking.data.data.local.SessionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tracking_db"
        ).build()

    @Provides
    fun provideSessionDao(db: AppDatabase): SessionDao =
        db.sessionDao()
}