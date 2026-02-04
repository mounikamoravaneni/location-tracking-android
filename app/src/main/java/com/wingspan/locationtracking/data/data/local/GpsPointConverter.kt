package com.wingspan.locationtracking.data.data.local

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.wingspan.locationtracking.domain.model.GpsPoint

class GpsPointConverter {

    private val gson = Gson()

    // Convert List<GpsPoint> → JSON String
    @TypeConverter
    fun fromGpsPointList(points: List<GpsPoint>): String {
        return gson.toJson(points)
    }

    // Convert JSON String → List<GpsPoint>
    @TypeConverter
    fun toGpsPointList(json: String): List<GpsPoint> {
        val type = object : TypeToken<List<GpsPoint>>() {}.type
        return gson.fromJson(json, type)
    }
}