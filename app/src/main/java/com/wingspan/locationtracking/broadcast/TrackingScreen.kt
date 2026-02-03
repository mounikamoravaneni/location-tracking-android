package com.wingspan.locationtracking.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.wingspan.locationtracking.services.TrackingService
import com.wingspan.locationtracking.viewmodel.TrackerViewModel

@Composable
fun TrackingScreen(
    viewModel: TrackerViewModel = hiltViewModel(),
) {

    val context = LocalContext.current

    DisposableEffect(Unit) {

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {

                if (intent?.action == TrackingService.ACTION_SESSION_COMPLETE) {

                    val start = intent.getLongExtra("startTime", 0)
                    val end = intent.getLongExtra("endTime", 0)
                    val distance = intent.getDoubleExtra("distance", 0.0)

                   // viewModel.saveSession(start, end, distance)
                }
            }
        }

        val filter = IntentFilter(TrackingService.ACTION_SESSION_COMPLETE)
        ContextCompat.registerReceiver(
            context,
            receiver,
            filter,
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        onDispose {
            context.unregisterReceiver(receiver)
        }
    }
}
