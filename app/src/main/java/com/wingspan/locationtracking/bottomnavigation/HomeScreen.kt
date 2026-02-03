package com.wingspan.locationtracking.bottomnavigation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wingspan.locationtracking.GradientButton
import com.wingspan.locationtracking.database.Session
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    isTracking: Boolean,
    lastSession: Session?,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onViewHistory: () -> Unit
) {


    // Compose-friendly permission states
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION
    )

    val notificationPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(android.Manifest.permission.POST_NOTIFICATIONS)
        } else null

    // Launcher for Settings result (optional, to detect user turning on GPS)
    val settingsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        // Nothing needed here, just user may turn on GPS
    }

    // 🔹 Location permission dialog
    if (!locationPermissionState.status.isGranted) {
        PermissionDialog(
            title = "Location Permission Needed",
            text = "We need location access to track your route.",
            onGrant = { locationPermissionState.launchPermissionRequest() }
        )
        return
    }

    // 🔹 Notification permission dialog (Android 13+)
    if (notificationPermissionState != null &&
        !notificationPermissionState.status.isGranted
    ) {
        PermissionDialog(
            title = "Notification Permission Needed",
            text = "Foreground service requires notification permission.",
            onGrant = { notificationPermissionState.launchPermissionRequest() }
        )
        return
    }

    // ✅ All checks passed → show main UI
    HomeContent(
        isTracking = isTracking,
        lastSession = lastSession,
        onStartTracking = onStartTracking,
        onStopTracking = onStopTracking,
        onViewHistory = onViewHistory,
        locationPermissionState = locationPermissionState,
        notificationPermissionState = notificationPermissionState,
        onTurnOnGps = {
            settingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }
    )
}

@SuppressLint("ServiceCast")
fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return false
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
}

@Composable
fun PermissionDialog(
    title: String,
    text: String,
    onGrant: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {},
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(onClick = onGrant) { Text("Grant") }
        }
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeContent(
    isTracking: Boolean,
    lastSession: Session?,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onViewHistory: () -> Unit,
    locationPermissionState: com.google.accompanist.permissions.PermissionState,
    notificationPermissionState: com.google.accompanist.permissions.PermissionState? = null,
    onTurnOnGps: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .systemBarsPadding(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top content
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Location Tracker",
                style = MaterialTheme.typography.headlineMedium
            )

            GradientButton(
                text = if (isTracking) "Stop Tracking" else "Start Tracking",
                onClick = {
                    if (isTracking) {
                        onStopTracking()
                    } else {
                        // 1️⃣ GPS check
                        if (!isLocationEnabled(context)) {
                            onTurnOnGps()
                            return@GradientButton
                        }

                        // 2️⃣ Location permission check dynamically (in case user revoked)
                        val locationGranted = context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                                android.content.pm.PackageManager.PERMISSION_GRANTED
                        if (!locationGranted) {
                            locationPermissionState.launchPermissionRequest()
                            return@GradientButton
                        }

                        // 3️⃣ Notification permission check (Android 13+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val notifGranted = context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) ==
                                    android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (!notifGranted) {
                                notificationPermissionState?.launchPermissionRequest()
                                return@GradientButton
                            }
                        }

                        // ✅ All OK → start tracking
                        onStartTracking()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            lastSession?.let { session ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Last Session", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Duration: ${session.duration / 1000} sec")
                        Text("Distance: ${"%.2f".format(session.distance)} m")
                    }
                }
            }
        }

        // Bottom button
        GradientButton(
            text = "View History",
            onClick = onViewHistory,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp)
        )
    }
}