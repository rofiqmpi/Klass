package com.mpi.klass

import androidx.compose.runtime.Composable

data class SensorPositionState(
    val xMeters: Float = 0f,
    val yMeters: Float = 0f,
    val headingDegrees: Float = 0f,
    val stepCount: Int = 0,
    val isSensorAvailable: Boolean = true,
)

@Composable
expect fun rememberCampusSensorTracker(active: Boolean): Pair<SensorPositionState, () -> Unit>
