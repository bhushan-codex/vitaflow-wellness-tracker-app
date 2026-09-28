package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activities")
data class ActivityEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // Walking, Running, Cycling, Workout, Other
    val durationMinutes: Int,
    val steps: Int = 0,
    val distanceMeters: Int = 0,
    val caloriesEstimate: Int = 0,
    val notes: String = "",
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "exercise_sessions")
data class ExerciseSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // Walking, Running, Cycling, Strength, Stretching, Yoga, Other
    val durationSeconds: Long,
    val notes: String = "",
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "water_entries")
data class WaterEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val amountMl: Int,
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "sleep_entries")
data class SleepEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bedtimeHour: Int,
    val bedtimeMinute: Int,
    val wakeHour: Int,
    val wakeMinute: Int,
    val durationMinutes: Int,
    val quality: String, // Excellent, Good, Okay, Poor
    val notes: String = "",
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "mood_entries")
data class MoodEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mood: String, // Great, Good, Okay, Low, Tired
    val note: String = "",
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "meal_entries")
data class MealEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val foodName: String,
    val note: String = "",
    val timeStr: String = "",
    val date: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "habits")
data class Habit(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String = "Movement", // Hydration, Movement, Mindfulness, Sleep, Nutrition, Personal
    val iconName: String = "check",
    val targetPerWeek: Int = 7,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "habit_completions", primaryKeys = ["habitId", "date"])
data class HabitCompletion(
    val habitId: Long,
    val date: String,
    val completedAt: Long = System.currentTimeMillis()
)
