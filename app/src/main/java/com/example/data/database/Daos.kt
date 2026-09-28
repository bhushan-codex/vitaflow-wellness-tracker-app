package com.example.data.database

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Query("SELECT * FROM activities ORDER BY timestamp DESC")
    fun getAllActivities(): Flow<List<ActivityEntry>>

    @Query("SELECT * FROM activities WHERE date = :date ORDER BY timestamp DESC")
    fun getActivitiesForDate(date: String): Flow<List<ActivityEntry>>

    @Query("SELECT * FROM activities WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC, timestamp ASC")
    fun getActivitiesBetweenDates(startDate: String, endDate: String): Flow<List<ActivityEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ActivityEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ActivityEntry>)

    @Update
    suspend fun update(entry: ActivityEntry)

    @Delete
    suspend fun delete(entry: ActivityEntry)

    @Query("DELETE FROM activities WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM activities")
    suspend fun deleteAll()
}

@Dao
interface ExerciseDao {
    @Query("SELECT * FROM exercise_sessions ORDER BY timestamp DESC")
    fun getAllSessions(): Flow<List<ExerciseSession>>

    @Query("SELECT * FROM exercise_sessions WHERE date = :date ORDER BY timestamp DESC")
    fun getSessionsForDate(date: String): Flow<List<ExerciseSession>>

    @Query("SELECT * FROM exercise_sessions WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSessionsBetweenDates(startDate: String, endDate: String): Flow<List<ExerciseSession>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: ExerciseSession): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<ExerciseSession>)

    @Delete
    suspend fun delete(session: ExerciseSession)

    @Query("DELETE FROM exercise_sessions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM exercise_sessions")
    suspend fun deleteAll()
}

@Dao
interface WaterDao {
    @Query("SELECT * FROM water_entries WHERE date = :date ORDER BY timestamp ASC")
    fun getWaterForDate(date: String): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getWaterBetweenDates(startDate: String, endDate: String): Flow<List<WaterEntry>>

    @Query("SELECT * FROM water_entries ORDER BY timestamp DESC")
    fun getAllWater(): Flow<List<WaterEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WaterEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WaterEntry>)

    @Delete
    suspend fun delete(entry: WaterEntry)

    @Query("DELETE FROM water_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM water_entries")
    suspend fun deleteAll()
}

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_entries WHERE date = :date ORDER BY timestamp DESC LIMIT 1")
    fun getSleepForDate(date: String): Flow<SleepEntry?>

    @Query("SELECT * FROM sleep_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSleepBetweenDates(startDate: String, endDate: String): Flow<List<SleepEntry>>

    @Query("SELECT * FROM sleep_entries ORDER BY timestamp DESC")
    fun getAllSleep(): Flow<List<SleepEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: SleepEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<SleepEntry>)

    @Delete
    suspend fun delete(entry: SleepEntry)

    @Query("DELETE FROM sleep_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM sleep_entries")
    suspend fun deleteAll()
}

@Dao
interface MoodDao {
    @Query("SELECT * FROM mood_entries WHERE date = :date ORDER BY timestamp DESC")
    fun getMoodForDate(date: String): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getMoodBetweenDates(startDate: String, endDate: String): Flow<List<MoodEntry>>

    @Query("SELECT * FROM mood_entries ORDER BY timestamp DESC")
    fun getAllMoods(): Flow<List<MoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MoodEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MoodEntry>)

    @Delete
    suspend fun delete(entry: MoodEntry)

    @Query("DELETE FROM mood_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM mood_entries")
    suspend fun deleteAll()
}

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_entries WHERE date = :date ORDER BY timestamp ASC")
    fun getMealsForDate(date: String): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getMealsBetweenDates(startDate: String, endDate: String): Flow<List<MealEntry>>

    @Query("SELECT * FROM meal_entries ORDER BY timestamp DESC")
    fun getAllMeals(): Flow<List<MealEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: MealEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<MealEntry>)

    @Delete
    suspend fun delete(entry: MealEntry)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM meal_entries")
    suspend fun deleteAll()
}

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY createdAt ASC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY createdAt ASC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabits(habits: List<Habit>)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("DELETE FROM habits WHERE id = :id")
    suspend fun deleteHabitById(id: Long)

    @Query("SELECT * FROM habit_completions WHERE date = :date")
    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE date BETWEEN :startDate AND :endDate")
    fun getCompletionsBetweenDates(startDate: String, endDate: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions")
    fun getAllCompletions(): Flow<List<HabitCompletion>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletion(completion: HabitCompletion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletions(completions: List<HabitCompletion>)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND date = :date")
    suspend fun deleteCompletion(habitId: Long, date: String)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId")
    suspend fun deleteCompletionsForHabit(habitId: Long)

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query("DELETE FROM habit_completions")
    suspend fun deleteAllCompletions()
}
