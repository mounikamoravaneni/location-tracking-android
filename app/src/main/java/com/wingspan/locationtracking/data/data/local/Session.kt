package com.wingspan.locationtracking.data.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.wingspan.locationtracking.domain.model.GpsPoint

@Entity(tableName = "sessions")
data class Session(

    @PrimaryKey
    val id: String,              // unique session id (timestamp based)

    val startTime: Long,         // when tracking started (System.currentTimeMillis)

    val endTime: Long,           // when tracking stopped

    val duration: Long,          // total duration in milliseconds

    val distance: Double,        // total distance in meters

    @TypeConverters(GpsPointConverter::class)
    val points: List<GpsPoint>  ,

    val tollAmount: Double? = null
)