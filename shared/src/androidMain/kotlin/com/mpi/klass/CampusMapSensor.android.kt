package com.mpi.klass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private const val STEP_LENGTH_METERS = 0.72f
private const val STEP_PEAK_THRESHOLD = 2.15f
private const val STEP_MIN_INTERVAL_MS = 380L

@Composable
actual fun rememberCampusSensorTracker(active: Boolean): Pair<SensorPositionState, () -> Unit> {
    val context = LocalContext.current
    var xMeters by remember { mutableFloatStateOf(0f) }
    var yMeters by remember { mutableFloatStateOf(0f) }
    var headingDeg by remember { mutableFloatStateOf(0f) }
    var stepCount by remember { mutableIntStateOf(0) }
    var sensorsAvailable by remember { mutableStateOf(true) }

    DisposableEffect(context, active) {
        if (!active) {
            return@DisposableEffect onDispose {}
        }
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        if (sensorManager == null) {
            sensorsAvailable = false
            return@DisposableEffect onDispose {}
        }

        val rotSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        val accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val magSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        sensorsAvailable = rotSensor != null || accelSensor != null

        val lastAccel = FloatArray(3)
        val lastMag = FloatArray(3)
        var hasAccel = false
        var hasMag = false
        val rotMatrix = FloatArray(9)
        val orientAngles = FloatArray(3)

        var filteredGravity = SensorManager.GRAVITY_EARTH
        var lastStepTimeMs = 0L

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                when (event.sensor.type) {
                    Sensor.TYPE_ROTATION_VECTOR -> {
                        SensorManager.getRotationMatrixFromVector(rotMatrix, event.values)
                        SensorManager.getOrientation(rotMatrix, orientAngles)
                        val deg = ((orientAngles[0] * 180.0 / PI).toFloat() + 360f) % 360f
                        headingDeg = deg
                    }
                    Sensor.TYPE_MAGNETIC_FIELD -> {
                        System.arraycopy(event.values, 0, lastMag, 0, 3)
                        hasMag = true
                    }
                    Sensor.TYPE_ACCELEROMETER -> {
                        System.arraycopy(event.values, 0, lastAccel, 0, 3)
                        hasAccel = true

                        if (rotSensor == null && hasMag) {
                            if (SensorManager.getRotationMatrix(rotMatrix, null, lastAccel, lastMag)) {
                                SensorManager.getOrientation(rotMatrix, orientAngles)
                                val deg = ((orientAngles[0] * 180.0 / PI).toFloat() + 360f) % 360f
                                headingDeg = deg
                            }
                        }

                        // Step peak dead-reckoning along current heading
                        val ax = event.values[0]
                        val ay = event.values[1]
                        val az = event.values[2]
                        val magnitude = sqrt(ax * ax + ay * ay + az * az)
                        filteredGravity = filteredGravity * 0.85f + magnitude * 0.15f
                        val delta = abs(magnitude - filteredGravity)
                        val now = System.currentTimeMillis()
                        if (delta > STEP_PEAK_THRESHOLD && (now - lastStepTimeMs) > STEP_MIN_INTERVAL_MS) {
                            lastStepTimeMs = now
                            val rad = headingDeg * (PI / 180.0)
                            xMeters += (STEP_LENGTH_METERS * sin(rad)).toFloat()
                            yMeters += (STEP_LENGTH_METERS * cos(rad)).toFloat()
                            stepCount += 1
                        }
                    }
                }
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }

        if (rotSensor != null) {
            sensorManager.registerListener(listener, rotSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if (accelSensor != null) {
            sensorManager.registerListener(listener, accelSensor, SensorManager.SENSOR_DELAY_UI)
        }
        if (rotSensor == null && magSensor != null) {
            sensorManager.registerListener(listener, magSensor, SensorManager.SENSOR_DELAY_UI)
        }

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val state = SensorPositionState(
        xMeters = xMeters,
        yMeters = yMeters,
        headingDegrees = headingDeg,
        stepCount = stepCount,
        isSensorAvailable = sensorsAvailable,
    )

    val resetOrigin = {
        xMeters = 0f
        yMeters = 0f
        stepCount = 0
    }

    return state to resetOrigin
}
