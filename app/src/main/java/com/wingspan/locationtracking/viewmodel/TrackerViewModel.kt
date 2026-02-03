package com.wingspan.locationtracking.viewmodel


import android.content.Context

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope


import com.wingspan.locationtracking.repository.SessionRepository
import com.wingspan.locationtracking.services.TrackingService
import com.wingspan.locationtracking.utils.TrackingState
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.wingspan.locationtracking.database.Session
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class TrackerViewModel @Inject constructor(
    private val repository: SessionRepository
) : ViewModel() {

    // 🔹 Tracking state using sealed class
    private val _trackingState =
        MutableStateFlow<TrackingState>(TrackingState.Idle)
    val trackingState: StateFlow<TrackingState> = _trackingState.asStateFlow()

    // 🔹 All sessions from DB
    val sessions: StateFlow<List<Session>> =
        repository.getAllSessions()
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())


    //map view
    private val _session = MutableStateFlow<Session?>(null)
    val session = _session.asStateFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _session.value = repository.getSessionById(sessionId)
            Log.d("points viewmodel","--->${repository.getSessionById(sessionId)}")
        }
    }

    // 🔹 Start tracking
    fun startTracking(context: Context) {
        Log.d("data viewmodel","startTracking")
        TrackingService.start(context)
        _trackingState.value = TrackingState.Tracking
    }

    // 🔹 Stop tracking
    fun stopTracking(context: Context) {
        Log.d("data viewmodel","stopTracking")
        viewModelScope.launch {
            TrackingService.stop(context)

            val points = TrackingService.getTrackedPoints()

            val lastSession = if (points.isNotEmpty()) {

                val startTime = TrackingService.getStartTime()
                val endTime = System.currentTimeMillis()

                val session = Session(
                    id = endTime.toString(),
                    startTime = startTime,
                    endTime = endTime,
                    duration = endTime - startTime,
                    distance = TrackingService.getDistance(),
                    points = points
                )
                Log.d("stop tracking","${session}")
                repository.insertSession(session)

                session
            } else null

            _trackingState.value = TrackingState.Stopped(lastSession)
            TrackingService.clearTrackedPoints()
        }
    }

    // 🔹 Get session by ID
    fun getSessionById(id: String): Session? {
        return sessions.value.find { it.id == id }
    }




}