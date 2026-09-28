package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ActivityEntry::class,
        ExerciseSession::class,
        WaterEntry::class,
        SleepEntry::class,
        MoodEntry::class,
        MealEntry::class,
        Habit::class,
        HabitCompletion::class
    ],
    version = 1,
    exportSchema = false
)
abstract class VitaFlowDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun waterDao(): WaterDao
    abstract fun sleepDao(): SleepDao
    abstract fun moodDao(): MoodDao
    abstract fun mealDao(): MealDao
    abstract fun habitDao(): HabitDao

    companion object {
        @Volatile
        private var INSTANCE: VitaFlowDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): VitaFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VitaFlowDatabase::class.java,
                    "vitaflow_database"
                )
                .addCallback(VitaFlowDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class VitaFlowDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialHabits(database.habitDao())
                    }
                }
            }

            private suspend fun populateInitialHabits(habitDao: HabitDao) {
                val defaultHabits = listOf(
                    Habit(name = "Drink 2L Water", category = "Hydration", iconName = "water", targetPerWeek = 7),
                    Habit(name = "30-min Daily Walk", category = "Movement", iconName = "walk", targetPerWeek = 5),
                    Habit(name = "Mindful Pause", category = "Mindfulness", iconName = "self_improvement", targetPerWeek = 7),
                    Habit(name = "Consistent Sleep Schedule", category = "Sleep", iconName = "bedtime", targetPerWeek = 7),
                    Habit(name = "Morning Stretch", category = "Movement", iconName = "fitness_center", targetPerWeek = 6)
                )
                habitDao.insertHabits(defaultHabits)
            }
        }
    }
}
