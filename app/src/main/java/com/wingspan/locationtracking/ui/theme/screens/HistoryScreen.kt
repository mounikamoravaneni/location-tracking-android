package com.wingspan.locationtracking.ui.theme.screens
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.wingspan.locationtracking.R
import com.wingspan.locationtracking.data.data.local.Session
import com.wingspan.locationtracking.ui.theme.DarkLavender
import com.wingspan.locationtracking.ui.theme.DarkSkyBlue
import com.wingspan.locationtracking.ui.theme.LightSkyBlue
import com.wingspan.locationtracking.ui.theme.SoftLavender
import com.wingspan.locationtracking.viewmodel.TollViewModel


import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    sessions: List<Session>,
    onSessionClick: (Session) -> Unit,
    onBack: () -> Unit,viewModel: TollViewModel= hiltViewModel()
) {
    var context = LocalContext.current
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tracking History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No sessions yet")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(sessions) { index, session ->
                    // Alternate card colors
                    val isDark = isSystemInDarkTheme()

                    val cardColor =
                        if (index % 2 == 0)
                            if (isDark) DarkSkyBlue else LightSkyBlue
                        else
                            if (isDark) DarkLavender else SoftLavender

                    val startPoint = session.points.firstOrNull()
                    val endPoint = session.points.lastOrNull()


                    val startAddress = startPoint?.let {
                        getShortAddress(context, it.latitude, it.longitude)
                    } ?: "N/A"

                    val endAddress = endPoint?.let {
                        getShortAddress(context, it.latitude, it.longitude)
                    } ?: "N/A"
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clickable { onSessionClick(session) }, // Only this for navigation
                        colors = CardDefaults.cardColors(
                            containerColor = cardColor
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .clickable {
                                    navController.navigate("sessionDetail/${session.id}")
                                }
                        ) {

                            Text(
                                text = formatDate(session.startTime),
                                style = MaterialTheme.typography.labelMedium
                            )

                            Text(
                                text = "Time: ${formatTo24HourTime(session.startTime)} - ${
                                    formatTo24HourTime(
                                        session.endTime
                                    )
                                }",
                                style = MaterialTheme.typography.titleMedium, fontSize = 12.sp
                            )

                            Text(
                                "Start Address: $startAddress",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                "End Address: $endAddress",
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text("Duration: ${session.duration / 1000} sec", fontSize = 12.sp)
                            Text("Distance: ${"%.2f".format(session.distance)} m", fontSize = 12.sp)

                            Row(
                                modifier = Modifier.padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp) // space between buttons
                            ) {
                                Button(
                                    onClick = {
                                        val file = exportSessionToCsv(context, session)
                                        shareFile(context, file)
                                    }
                                ) {
                                    Text(
                                        text = "Export CSV",
                                        fontSize = 12.sp
                                    )
                                }

                                Button(
                                    onClick = {
                                        val points = session.points
                                        Log.d("data", "-->${points.size} points")

                                        if (points.size < 2) {
                                            Log.d("Toll", "Only one GPS point, skipping TollTally API")
                                            return@Button
                                        }

                                        // Convert points to polyline string
                                        val polyencode = PolyUtil.encode(points.map { LatLng(it.latitude, it.longitude) })



                                        // Call ViewModel
                                        val apiKey = context.getString(R.string.tolltally_api_key)
                                        viewModel.fetchToll(points, apiKey,polyencode)
                                    },
                                    enabled = session.tollAmount == null // disable if already fetched
                                ) {
                                    Text(
                                        text = if (session.tollAmount == null) "Fetch Toll" else "Toll Fetched",
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}

fun formatDate(timeMillis: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}

//store file to app private storage
fun exportSessionToCsv(context: Context, session: Session): File {

    val fileName = "session_${session.id}.csv"
    val file = File(context.getExternalFilesDir(null), fileName)

    val startPoint = session.points.firstOrNull()
    val endPoint = session.points.lastOrNull()

    val startAddress = startPoint?.let {
        getShortAddress(context, it.latitude, it.longitude)
    } ?: "N/A"

    val endAddress = endPoint?.let {
        getShortAddress(context, it.latitude, it.longitude)
    } ?: "N/A"

    val duration = formatDuration(session.endTime - session.startTime)
    val distanceKm = metersToKm(session.distance)

    file.bufferedWriter().use { writer ->
        writer.write(
            "Session ID,Start Address,End Address,Start Date.Time,End Date.Time,Duration,Distance (km)\n"
        )
        writer.write(
            "${session.id}," +
                    "\"$startAddress\"," +
                    "\"$endAddress\"," +
                    "${formatTime(session.startTime)}," +
                    "${formatTime(session.endTime)}," +
                    "$duration," +
                    "$distanceKm\n"
        )
    }

    return file
}


@SuppressLint("DefaultLocale")
fun metersToKm(meters: Double): String =
    String.format("%.2f", meters / 1000.0)

fun getShortAddress(context: Context, lat: Double, lng: Double): String {
    return try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val address = geocoder.getFromLocation(lat, lng, 1)?.firstOrNull()

        val subLocality = address?.subLocality
        val locality = address?.locality

        when {
            !subLocality.isNullOrEmpty() && !locality.isNullOrEmpty() ->
                "$subLocality, $locality"

            !locality.isNullOrEmpty() ->
                locality

            else ->
                "Unknown area"
        }
    } catch (e: Exception) {
        "Unknown area"
    }
}


fun shareFile(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.provider",
        file
    )

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }

    context.startActivity(
        Intent.createChooser(intent, "Share session CSV")
    )
}


fun formatTo24HourTime(timeMillis: Long): String {
    val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timeMillis))
}
