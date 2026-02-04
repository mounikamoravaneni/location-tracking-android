package com.wingspan.locationtracking.ui.theme.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.wingspan.locationtracking.ui.theme.components.GradientButton
import com.wingspan.locationtracking.data.data.local.Session
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    isTracking: Boolean,
    sessions: List<Session>,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onViewHistory: () -> Unit
) {

    Log.d("data homescreen","--${sessions}")
    // Compose-friendly permission states
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    val notificationPermissionState =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
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
        sessions = sessions,
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
    sessions: List<Session>,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onViewHistory: () -> Unit,
    locationPermissionState: PermissionState,
    notificationPermissionState: PermissionState? = null,
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
                .weight(1f),

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
                        val locationGranted = context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                                PackageManager.PERMISSION_GRANTED
                        if (!locationGranted) {
                            locationPermissionState.launchPermissionRequest()
                            return@GradientButton
                        }

                        // 3️⃣ Notification permission check (Android 13+)
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            val notifGranted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                                    PackageManager.PERMISSION_GRANTED
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
            Spacer(modifier = Modifier.height(24.dp))
            // 🔹 Session History Header
            if (sessions.isNotEmpty()) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Session History",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "View all →",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { onViewHistory() }
                    )
                }



                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    itemsIndexed(sessions.take(2)) { index, session ->

                        val cardColor = when (index) {
                            0 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                            1 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.08f)
                            else -> MaterialTheme.colorScheme.surface
                        }

                        SessionCard(
                            session = session,
                            backgroundColor = cardColor
                        )
                    }

                }


            } else {
                Text(
                    text = "No sessions yet",
                    style = MaterialTheme.typography.bodyMedium
                )
            }


        }


    }
}
@Composable
fun SessionCard(
    session: Session,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember { java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()) }
    val sessionDate = dateFormatter.format(java.util.Date(session.startTime))

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Show Date
            Text(
                text = sessionDate,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Duration
            Text(
                text = "Duration: ${formatDuration(session.duration)}",
                style = MaterialTheme.typography.bodySmall
            )

            // Distance
            Text(
                text = "Distance: ${"%.2f".format(session.distance)} m",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

