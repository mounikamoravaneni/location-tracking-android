package com.wingspan.locationtracking.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.*
import com.wingspan.locationtracking.R
import com.wingspan.locationtracking.database.AppDatabase
import com.wingspan.locationtracking.database.Session
import com.wingspan.locationtracking.model.GpsPoint
import com.wingspan.locationtracking.repository.SessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TrackingService : Service() {

    @Inject
    lateinit var repository: SessionRepository
    companion object {
        const val ACTION_SESSION_COMPLETE = "ACTION_SESSION_COMPLETE"
        private const val CHANNEL_ID = "tracking_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "START_TRACKING"
        const val ACTION_STOP = "STOP_TRACKING"
        // Keep track of GPS points in memory
        private val trackedPoints = mutableListOf<GpsPoint>()
        private var startTime: Long = 0L
        private var endTime: Long = 0L
        private var distance: Double = 0.0
        fun getStartTime(): Long = startTime

        fun getTrackedPoints(): List<GpsPoint> = trackedPoints.toList()

        fun clearTrackedPoints() {
            trackedPoints.clear()
            startTime = 0L
            endTime = 0L
        }

        //calling service here
        fun start(context: Context) {
            val intent = Intent(context, TrackingService::class.java)
            intent.action = ACTION_START
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, TrackingService::class.java)
            intent.action = ACTION_STOP
            context.startService(intent)
        }



        fun getDistance(): Double {
            if (trackedPoints.size < 2) return 0.0
            var distance = 0.0
            for (i in 1 until trackedPoints.size) {
                val p1 = trackedPoints[i - 1]
                val p2 = trackedPoints[i]
                val result = FloatArray(1)
                Location.distanceBetween(
                    p1.latitude, p1.longitude,
                    p2.latitude, p2.longitude,
                    result
                )
                distance += result[0]
            }
            return distance
        }
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback


    override fun onCreate() {
        super.onCreate()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
            .setMinUpdateDistanceMeters(2f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)
                result.locations.forEach { location ->
                    trackedPoints.add(
                        GpsPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundService()
            ACTION_STOP -> {  Log.d("TrackingService", "Stop clicked")

                val endTime = System.currentTimeMillis()
                val totalDistance = getDistance()
                saveSession(startTime, endTime,totalDistance )

                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {
        //createNotificationChannel()   // 🔴 MUST
        startTime = System.currentTimeMillis()
        startForeground(
            NOTIFICATION_ID,
            createNotification("Tracking location...")
        )
        startLocationUpdates()
    }

    private fun stopTracking() {
        stopLocationUpdates()
        endTime = System.currentTimeMillis()

        val intent = Intent(ACTION_SESSION_COMPLETE).apply {
            putExtra("startTime", startTime)
            putExtra("endTime", endTime)
            putExtra("distance", distance)
        }

        sendBroadcast(intent)

        stopForeground(true)
        stopSelf()
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                null
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun createNotification(contentText: String): Notification {

        // 👉 Stop Button Intent
        val stopIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            100,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Location Mounika")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // replace with your icon
            .setOngoing(true)
            // ⭐ STOP BUTTON
            .addAction(
                R.drawable.ic_stop,   // create stop icon OR use launcher icon
                ACTION_STOP,
                stopPendingIntent
            )
            .build()
    }



    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Location Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Tracks location in background"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    override fun onBind(intent: Intent?): IBinder? = null

    fun saveSession(start: Long, end: Long, distance: Double) {

        Log.d("TrackingVM", "saveSession() called")
        Log.d("TrackingVM", "StartTime = $start")
        Log.d("TrackingVM", "EndTime = $end")
        Log.d("TrackingVM", "Distance = $distance")

        CoroutineScope(Dispatchers.IO).launch{

            val points = TrackingService.getTrackedPoints()

            Log.d("TrackingVM", "Points Count = ${points.size}")

            val session = Session(
                id = end.toString(),
                startTime = start,
                endTime = end,
                duration = end - start,
                distance = distance,
                points = points
            )

            Log.d("TrackingVM", "Session Object = $session")

            repository.insertSession(session)

            Log.d("TrackingVM", "Session Inserted Into Room DB")

            TrackingService.clearTrackedPoints()

            Log.d("TrackingVM", "Tracked Points Cleared")
        }
}

}