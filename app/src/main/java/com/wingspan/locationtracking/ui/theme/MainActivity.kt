package com.wingspan.locationtracking.ui.theme

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.wingspan.locationtracking.ui.theme.screens.HistoryScreen
import com.wingspan.locationtracking.ui.theme.screens.HomeScreen
import com.wingspan.locationtracking.ui.theme.screens.MapViewScreen
import com.wingspan.locationtracking.ui.theme.screens.SessionDetailScreen
import com.wingspan.locationtracking.utils.TrackingState
import com.wingspan.locationtracking.viewmodel.TrackerViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LocationTrackingTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val navController = rememberNavController()
                    val trackerViewModel: TrackerViewModel = hiltViewModel()

                    val trackingState =
                        trackerViewModel.trackingState.collectAsState().value

                    val isTracking = trackingState is TrackingState.Tracking
                    val lastSession =
                        (trackingState as? TrackingState.Stopped)?.lastSession

                    Log.d("MainActivity", "Is tracking: $isTracking")

                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {

                        composable("home") {
                            val sessions =
                                trackerViewModel.sessions.collectAsState().value

                            HomeScreen(
                                navController = navController,
                                isTracking = isTracking,
                                sessions = sessions,
                                onStartTracking = {
                                    trackerViewModel.startTracking(this@MainActivity)
                                },
                                onStopTracking = {
                                    trackerViewModel.stopTracking(this@MainActivity)
                                },
                                onViewHistory = {
                                    navController.navigate("history")
                                }
                            )
                        }

                        composable("history") {
                            val sessions =
                                trackerViewModel.sessions.collectAsState().value

                            HistoryScreen(
                                navController = navController,
                                sessions = sessions,
                                onSessionClick = { session ->
                                    navController.navigate(
                                        "sessionDetail/${session.id}"
                                    )
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable(
                            route = "sessionDetail/{sessionId}",
                            arguments = listOf(
                                navArgument("sessionId") {
                                    type = NavType.StringType
                                }
                            )
                        ) { backStackEntry ->

                            val sessionId =
                                backStackEntry.arguments
                                    ?.getString("sessionId")
                                    .orEmpty()

                            val session =
                                trackerViewModel.getSessionById(sessionId)

                            SessionDetailScreen(
                                session = session,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("mapView/{sessionId}") { backStackEntry ->
                            val sessionId =
                                backStackEntry.arguments
                                    ?.getString("sessionId")
                                    .orEmpty()

                            MapViewScreen(
                                sessionId = sessionId
                            )
                        }
                    }
                }
            }
        }
    }
}