package com.example.data.sensor

import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class StepSensorManager(private val context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _isSensorAvailable = MutableStateFlow(stepSensor != null)
    val isSensorAvailable: StateFlow<Boolean> = _isSensorAvailable.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _sensorSteps = MutableStateFlow(0)
    val sensorSteps: StateFlow<Int> = _sensorSteps.asStateFlow()

    private var initialStepOffset = -1

    fun hasPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    fun startListening() {
        if (stepSensor == null || !hasPermission()) return
        if (_isListening.value) return

        try {
            val registered = sensorManager?.registerListener(
                this,
                stepSensor,
                SensorManager.SENSOR_DELAY_UI
            ) ?: false
            _isListening.value = registered
        } catch (_: Exception) {
            _isListening.value = false
        }
    }

    fun stopListening() {
        if (!_isListening.value) return
        try {
            sensorManager?.unregisterListener(this)
        } catch (_: Exception) {
        } finally {
            _isListening.value = false
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER && event.values.isNotEmpty()) {
            val totalRawSteps = event.values[0].toInt()
            if (initialStepOffset < 0) {
                initialStepOffset = totalRawSteps
            }
            val currentSteps = (totalRawSteps - initialStepOffset).coerceAtLeast(0)
            _sensorSteps.value = currentSteps
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No-op
    }
}
