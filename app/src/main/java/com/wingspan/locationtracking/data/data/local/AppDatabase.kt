package com.wingspan.locationtracking.data.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wingspan.locationtracking.data.data.local.SessionDao

@Database(
    entities = [Session::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(GpsPointConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}