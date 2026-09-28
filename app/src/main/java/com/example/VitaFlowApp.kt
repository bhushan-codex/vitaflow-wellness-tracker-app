package com.example

import android.app.Application
import com.example.data.database.VitaFlowDatabase
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.WellnessRepository
import com.example.data.sensor.StepSensorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class VitaFlowApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    val database by lazy { VitaFlowDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { WellnessRepository(database) }
    val preferencesRepository by lazy { UserPreferencesRepository(this) }
    val stepSensorManager by lazy { StepSensorManager(this) }

    override fun onCreate() {
        super.onCreate()
    }
}
