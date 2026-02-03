package com.wingspan.locationtracking.ui.theme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.wingspan.locationtracking.OpenStreetMapView
import com.wingspan.locationtracking.model.GpsPoint
import com.wingspan.locationtracking.viewmodel.TrackerViewModel
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun SessionDetailScreen1(
    sessionId: String,
    navController: NavHostController,
    viewModel: TrackerViewModel = hiltViewModel()
) {

    val session by viewModel.session.collectAsState()

    val fakePoints = listOf(
        GpsPoint(17.4415969, 78.4472591, 0),
        GpsPoint(17.4419000, 78.4478000, 0),
        GpsPoint(17.4423000, 78.4484000, 0),
        GpsPoint(17.4428000, 78.4490000, 0),
        GpsPoint(17.4433000, 78.4496000, 0),
        GpsPoint(17.4439000, 78.4502000, 0)
    )

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    session?.let { sessionData ->

//        val mapPoints = if ((sessionData.distance ?: 0.0) < 50 || sessionData.points.isNullOrEmpty()) {
//            fakePoints
//        } else {
//            sessionData.points
//        }
        val mapPoints = sessionData.points.orEmpty()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {

            // 🗺️ MAP AS BACKGROUND

            if (mapPoints.isNotEmpty()) {
                OpenStreetMapView(
                    points = mapPoints,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                NoRouteView()
            }


            // 🌈 SESSION INFO CARD (FLOATING)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.96f)
                )
            ) {

                Column(modifier = Modifier.padding(16.dp)) {

                    Text(
                        text = "Session Summary",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E88E5) // Blue accent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        InfoItem("Distance", "${"%.2f".format((sessionData.distance ?: 0.0) / 1000)} km")
                        InfoItem("Duration", formatDuration(sessionData.duration ?: 0))
                        InfoItem("Points", mapPoints.size.toString())
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Start: ${formatTime(sessionData.startTime ?: 0)}",
                        fontSize = 13.sp
                    )
                    Text(
                        text = "End: ${formatTime(sessionData.endTime ?: 0)}",
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
@Composable
fun InfoItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2E7D32) // Green
        )
        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Gray
        )
    }
}
fun formatTime(time: Long): String {
    if (time <= 0L) return "N/A"

    val sdf = SimpleDateFormat("dd MMM yyyy • hh:mm a", Locale.getDefault())
    return sdf.format(Date(time))
}
fun formatDuration(duration: Long): String {
    if (duration <= 0L) return "00:00:00"

    val totalSeconds = duration / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return String.format(
        Locale.getDefault(),
        "%02d:%02d:%02d",
        hours, minutes, seconds
    )
}
@Composable
fun NoRouteView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No GPS route available",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "This session has no recorded points",
                fontSize = 13.sp,
                color = Color.LightGray
            )
        }
    }
}
