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
                Surface(modifier = Modifier.Companion.fillMaxSize()) {
                    val navController = rememberNavController()
                    val trackerViewModel: TrackerViewModel = hiltViewModel()
                    val trackingState =
                        trackerViewModel.trackingState.collectAsState().value

                    val isTracking = trackingState is TrackingState.Tracking
                    val lastSession =
                        (trackingState as? TrackingState.Stopped)?.lastSession
                    Log.d("data mainactivity", "${isTracking}")
                    NavHost(
                        navController = navController,
                        startDestination = "home"
                    ) {

                        composable("home") {
                            val sessions =
                                trackerViewModel.sessions.collectAsState().value
                            HomeScreen(
                                navController,
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
                                navController,
                                sessions = sessions,
                                onSessionClick = { session ->
                                    navController.navigate("sessionDetail/${session.id}")
                                },
                                onBack = { navController.popBackStack() }
                            )

                        }

                        composable(
                            "sessionDetail/{sessionId}",
                            arguments = listOf(
                                navArgument("sessionId") {
                                    type = NavType.Companion.StringType
                                }
                            )
                        ) { backStackEntry ->
                            val sessionId =
                                backStackEntry.arguments?.getString("sessionId") ?: ""

                            val session =
                                trackerViewModel.getSessionById(sessionId)

                            SessionDetailScreen(
                                session = session,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            "sessionDetail/{sessionId}"
                        ) { backStack ->

                            val sessionId = backStack.arguments?.getString("sessionId") ?: ""

                            MapViewScreen(
                                sessionId = sessionId,
                                navController = navController
                            )
                        }
                    }
                }

            }

        }

    }
}