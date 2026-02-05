package com.wingspan.locationtracking.map

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.wingspan.locationtracking.domain.model.GpsPoint
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline

@Composable
fun OpenStreetMapView(
    points: List<GpsPoint>,
    modifier: Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            val map = MapView(context).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)
                controller.setZoom(16.0)
            }

            if (points.isNotEmpty()) {
                val geoPoints = points.map {
                    GeoPoint(it.latitude, it.longitude)
                }

                // Center map
                map.controller.setCenter(geoPoints.first())

                // Polyline
                val polyline = Polyline().apply {
                    setPoints(geoPoints)
                    outlinePaint.color = Color.RED
                    outlinePaint.strokeWidth = 8f
                }
                map.overlays.add(polyline)

                // Start marker
                val startMarker = Marker(map).apply {
                    position = geoPoints.first()
                    title = "Start"
                }
                map.overlays.add(startMarker)

                // End marker
                val endMarker = Marker(map).apply {
                    position = geoPoints.last()
                    title = "End"
                }
                map.overlays.add(endMarker)
            }

            map
        }
    )
}
