package com.wingspan.locationtracking

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.wingspan.locationtracking.model.GpsPoint

@Composable
fun OpenStreetMapView(
    points: List<GpsPoint>,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val map = org.osmdroid.views.MapView(context).apply {
                setMultiTouchControls(true)
                controller.setZoom(16.0)
            }

            if (points.isNotEmpty()) {
                val geoPoints = points.map {
                    org.osmdroid.util.GeoPoint(it.latitude, it.longitude)
                }

                // Center map
                map.controller.setCenter(geoPoints.first())

                // Polyline
                val polyline = org.osmdroid.views.overlay.Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.color = android.graphics.Color.RED
                    outlinePaint.strokeWidth = 8f
                }
                map.overlays.add(polyline)

                // Start marker
                val startMarker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = geoPoints.first()
                    title = "Start"
                }
                map.overlays.add(startMarker)

                // End marker
                val endMarker = org.osmdroid.views.overlay.Marker(map).apply {
                    position = geoPoints.last()
                    title = "End"
                }
                map.overlays.add(endMarker)
            }

            map
        }
    )
}
