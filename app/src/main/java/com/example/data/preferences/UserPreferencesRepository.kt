package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "vitaflow_preferences")

data class UserGoals(
    val stepGoal: Int = 7500,
    val waterGoalMl: Int = 2000,
    val sleepGoalMinutes: Int = 480, // 8 hours
    val exerciseGoalMinutes: Int = 30,
    val habitsGoalCount: Int = 3
)

class UserPreferencesRepository(private val context: Context) {
    companion object {
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_STEP_GOAL = intPreferencesKey("goal_steps")
        val KEY_WATER_GOAL = intPreferencesKey("goal_water_ml")
        val KEY_SLEEP_GOAL = intPreferencesKey("goal_sleep_mins")
        val KEY_EXERCISE_GOAL = intPreferencesKey("goal_exercise_mins")
        val KEY_HABITS_GOAL = intPreferencesKey("goal_habits_count")
        val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val KEY_WATER_REMINDERS = booleanPreferencesKey("reminder_water")
        val KEY_MOVEMENT_REMINDERS = booleanPreferencesKey("reminder_movement")
        val KEY_SLEEP_REMINDERS = booleanPreferencesKey("reminder_sleep")
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "system", "light", "dark"
    }

    val isOnboardingCompleted: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences ->
            preferences[KEY_ONBOARDING_COMPLETED] ?: false
        }

    val userName: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_USER_NAME] ?: "Friend" }

    val userGoals: Flow<UserGoals> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { preferences ->
            UserGoals(
                stepGoal = preferences[KEY_STEP_GOAL] ?: 7500,
                waterGoalMl = preferences[KEY_WATER_GOAL] ?: 2000,
                sleepGoalMinutes = preferences[KEY_SLEEP_GOAL] ?: 480,
                exerciseGoalMinutes = preferences[KEY_EXERCISE_GOAL] ?: 30,
                habitsGoalCount = preferences[KEY_HABITS_GOAL] ?: 3
            )
        }

    val themeMode: Flow<String> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_THEME_MODE] ?: "system" }

    val notificationsEnabled: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_NOTIFICATIONS_ENABLED] ?: false }

    val waterReminders: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_WATER_REMINDERS] ?: true }

    val movementReminders: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_MOVEMENT_REMINDERS] ?: true }

    val sleepReminders: Flow<Boolean> = context.dataStore.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { it[KEY_SLEEP_REMINDERS] ?: true }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { it[KEY_USER_NAME] = name.trim() }
    }

    suspend fun updateGoals(stepGoal: Int, waterGoalMl: Int, sleepHours: Float, exerciseMins: Int, habitCount: Int) {
        context.dataStore.edit {
            it[KEY_STEP_GOAL] = stepGoal.coerceIn(1000, 50000)
            it[KEY_WATER_GOAL] = waterGoalMl.coerceIn(500, 6000)
            it[KEY_SLEEP_GOAL] = (sleepHours * 60).toInt().coerceIn(180, 840)
            it[KEY_EXERCISE_GOAL] = exerciseMins.coerceIn(5, 300)
            it[KEY_HABITS_GOAL] = habitCount.coerceIn(1, 10)
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { it[KEY_THEME_MODE] = mode }
    }

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS_ENABLED] = enabled }
    }

    suspend fun setWaterReminders(enabled: Boolean) {
        context.dataStore.edit { it[KEY_WATER_REMINDERS] = enabled }
    }

    suspend fun setMovementReminders(enabled: Boolean) {
        context.dataStore.edit { it[KEY_MOVEMENT_REMINDERS] = enabled }
    }

    suspend fun setSleepReminders(enabled: Boolean) {
        context.dataStore.edit { it[KEY_SLEEP_REMINDERS] = enabled }
    }

    suspend fun clearPreferences() {
        context.dataStore.edit { it.clear() }
    }
}
