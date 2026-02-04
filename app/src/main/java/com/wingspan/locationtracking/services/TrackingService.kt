package com.wingspan.locationtracking.services

import android.annotation.SuppressLint
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
import com.google.android.gms.location.*
import com.wingspan.locationtracking.R
import com.wingspan.locationtracking.data.data.local.Session
import com.wingspan.locationtracking.domain.model.GpsPoint
import com.wingspan.locationtracking.data.data.repository.SessionRepository

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
        private var shouldSaveSession = false


        // Keep track of GPS points in memory
        private val trackedPoints = mutableListOf<GpsPoint>()
        private var startTime: Long = 0L
        private var endTime: Long = 0L

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

        fun setSaveSessionFlag(value: Boolean) {
            shouldSaveSession = value
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

                    // Ignore poor GPS points
                    if (location.accuracy > 20f) {
                        Log.d("GPS", "Poor accuracy: ${location.accuracy}m")
                        return@forEach
                    }
                    trackedPoints.add(
                        GpsPoint(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                }
                updateNotification()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> startForegroundService()
            ACTION_STOP -> {  Log.d("TrackingService", "Stop clicked")
                // Check if intent has extra to indicate it came from notification
                val fromNotification = intent.getBooleanExtra("FROM_NOTIFICATION", false)
                if (fromNotification) {
                    setSaveSessionFlag(true)
                }
                if (shouldSaveSession) {
                    val endTime = System.currentTimeMillis()
                    val totalDistance = getDistance()
                    saveSession(startTime, endTime, totalDistance)
                }

                stopForeground(true)
                stopSelf()
            }
        }
        return START_STICKY
    }

    private fun startForegroundService() {

        startTime = System.currentTimeMillis()
        startForeground(
            NOTIFICATION_ID,
            createNotification("00:00", "0.0 km")
        )
        startLocationUpdates()
    }

//    private fun stopTracking() {
//        stopLocationUpdates()
//        endTime = System.currentTimeMillis()
//
//        val intent = Intent(ACTION_SESSION_COMPLETE).apply {
//            putExtra("startTime", startTime)
//            putExtra("endTime", endTime)
//            putExtra("distance", distance)
//        }
//
//        sendBroadcast(intent)
//
//        stopForeground(true)
//        stopSelf()
//    }

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


private fun updateNotification() {
    val duration = formatDuration(System.currentTimeMillis() - startTime)
    val distance = "%.2f km".format(calculateDistance() / 1000)

    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    manager.notify(
        NOTIFICATION_ID,
        createNotification(duration, distance)
    )
}
    private fun calculateDistance(): Double {
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
    private fun createNotification(duration: String,
                                     distance: String): Notification {

        // 👉 Stop Button Intent
        val stopIntent = Intent(this, TrackingService::class.java).apply {
            action = ACTION_STOP
            putExtra("FROM_NOTIFICATION", true)
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
            .setContentTitle("Location Tracking")
            .setContentText("Duration: $duration • Distance: $distance")
            .setSmallIcon(R.drawable.notification_icon) // replace with your icon
            .setOngoing(true)
            //  STOP BUTTON
            .addAction(
                R.drawable.ic_stop,
                ACTION_STOP,
                stopPendingIntent
            )
            .build()
    }



    override fun onBind(intent: Intent?): IBinder? = null

    fun saveSession(start: Long, end: Long, distance: Double) {

        Log.d("TrackingVM", "saveSession() called")
        Log.d("TrackingVM", "StartTime = $start")
        Log.d("TrackingVM", "EndTime = $end")
        Log.d("TrackingVM", "Distance = $distance")

        CoroutineScope(Dispatchers.IO).launch{

            val points = getTrackedPoints()

            Log.d("TrackingVM", "Points Count = ${points.size}")

            val session = Session(
                id = end.toString(),
                startTime = start,
                endTime = end,
                duration = end - start,
                distance = distance,
                points = points
            )

            Log.d("data service stop","--${session}")

            repository.insertSession(session)

            Log.d("TrackingVM", "Session Inserted Into Room DB")

           clearTrackedPoints()

            Log.d("TrackingVM", "Tracked Points Cleared")
        }
    }

}
@SuppressLint("DefaultLocale")
fun formatDuration(durationMillis: Long): String {
    val totalSeconds = durationMillis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
