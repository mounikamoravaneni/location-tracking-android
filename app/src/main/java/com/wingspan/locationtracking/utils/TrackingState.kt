package com.wingspan.locationtracking.utils

import com.wingspan.locationtracking.database.Session



sealed class TrackingState {
    object Idle : TrackingState()                   // Not tracking
    object Tracking : TrackingState()              // Currently tracking
    data class Stopped(val lastSession: Session?) : TrackingState() // Tracking stopped, optional last session
}