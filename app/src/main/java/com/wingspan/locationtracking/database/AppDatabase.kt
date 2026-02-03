package com.wingspan.locationtracking.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [Session::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(GpsPointConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}
