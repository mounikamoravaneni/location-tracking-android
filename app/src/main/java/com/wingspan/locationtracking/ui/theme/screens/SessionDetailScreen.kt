package com.wingspan.locationtracking.ui.theme.screens
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import androidx.compose.ui.unit.dp
import com.wingspan.locationtracking.data.data.local.Session
import com.wingspan.locationtracking.map.OpenStreetMapView
import com.wingspan.locationtracking.ui.theme.LightSkyBlue
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionDetailScreen(
    session: Session?,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Session") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->

        session?.let { data ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {

                // 🗺️ MAP BACKGROUND
                if (data.points.isNotEmpty()) {
                    OpenStreetMapView(
                        points = data.points,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // 📊 INFO CARD (BOTTOM)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {

                        Text(
                            "Session Summary",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Divider()

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            InfoItem("Distance", "${"%.2f".format(data.distance / 1000)} km")
                            InfoItem("Duration", formatDuration(data.duration))
                            InfoItem("Points", data.points.size.toString())
                        }

                        Divider()

                        Text(
                            "Start: ${Date(data.startTime)}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Text(
                            "End: ${Date(data.endTime)}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Session not found")
        }
    }
}
