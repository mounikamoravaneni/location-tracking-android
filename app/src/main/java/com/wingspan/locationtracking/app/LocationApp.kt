package com.wingspan.locationtracking.app

import android.app.Application
import android.preference.PreferenceManager
import dagger.hilt.android.HiltAndroidApp
import org.osmdroid.config.Configuration

@HiltAndroidApp
class LocationApp : Application() {
    override fun onCreate() {
        super.onCreate()

        Configuration.getInstance().load(
            this,
            PreferenceManager.getDefaultSharedPreferences(this)
        )
    }
}