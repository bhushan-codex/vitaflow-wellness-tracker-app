package com.example.data.repository

import com.example.data.database.VitaFlowDatabase
import com.example.data.model.*
import com.example.data.preferences.UserGoals
import com.example.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

data class TodayWellnessSummary(
    val date: String,
    val totalSteps: Int,
    val totalWaterMl: Int,
    val totalExerciseSeconds: Long,
    val sleepEntry: SleepEntry?,
    val latestMood: MoodEntry?,
    val activeHabits: List<Habit>,
    val completedHabitIds: Set<Long>,
    val meals: List<MealEntry>,
    val activities: List<ActivityEntry>,
    val balanceScore: Int // 0..100
)

data class DaySummaryItem(
    val date: String,
    val hasActivity: Boolean,
    val hasWater: Boolean,
    val hasSleep: Boolean,
    val hasHabit: Boolean,
    val totalSteps: Int,
    val totalWaterMl: Int,
    val balanceScore: Int
)

data class WeeklyInsights(
    val daysWithActivity: Int,
    val daysWithWaterGoal: Int,
    val daysWithSleepLogged: Int,
    val totalExerciseMinutes: Int,
    val totalHabitsCompleted: Int,
    val currentStreakDays: Int,
    val activityConsistencyText: String,
    val exerciseTrendText: String,
    val sleepTrendText: String,
    val waterTrendText: String,
    val habitTrendText: String
)

class WellnessRepository(
    private val database: VitaFlowDatabase
) {
    private val activityDao = database.activityDao()
    private val exerciseDao = database.exerciseDao()
    private val waterDao = database.waterDao()
    private val sleepDao = database.sleepDao()
    private val moodDao = database.moodDao()
    private val mealDao = database.mealDao()
    private val habitDao = database.habitDao()

    fun getTodaySummary(goals: UserGoals): Flow<TodayWellnessSummary> {
        val today = DateTimeUtils.todayString()

        val activitiesFlow = activityDao.getActivitiesForDate(today)
        val exerciseFlow = exerciseDao.getSessionsForDate(today)
        val waterFlow = waterDao.getWaterForDate(today)
        val sleepFlow = sleepDao.getSleepForDate(today)
        val moodFlow = moodDao.getMoodForDate(today)
        val mealsFlow = mealDao.getMealsForDate(today)
        val habitsFlow = habitDao.getActiveHabits()
        val completionsFlow = habitDao.getCompletionsForDate(today)

        return combine(
            activitiesFlow,
            exerciseFlow,
            waterFlow,
            sleepFlow,
            moodFlow
        ) { acts, exers, waters, sleep, moods ->
            CombinedGroup1(acts, exers, waters, sleep, moods)
        }.combine(
            combine(mealsFlow, habitsFlow, completionsFlow) { meals, habits, comps ->
                CombinedGroup2(meals, habits, comps)
            }
        ) { g1, g2 ->
            val totalSteps = g1.activities.sumOf { it.steps }
            val totalWater = g1.waters.sumOf { it.amountMl }
            val totalExerciseSec = g1.exercises.sumOf { it.durationSeconds }
            val completedSet = g2.completions.map { it.habitId }.toSet()

            // Calculate non-medical Daily Balance score (0..100)
            var score = 0
            // 1. Steps progress (up to 20 pts)
            if (goals.stepGoal > 0) {
                val stepFraction = (totalSteps.toFloat() / goals.stepGoal).coerceAtMost(1f)
                score += (stepFraction * 20).toInt()
            }
            // 2. Hydration progress (up to 20 pts)
            if (goals.waterGoalMl > 0) {
                val waterFraction = (totalWater.toFloat() / goals.waterGoalMl).coerceAtMost(1f)
                score += (waterFraction * 20).toInt()
            }
            // 3. Sleep logged & reasonable duration (up to 20 pts)
            if (g1.sleep != null) {
                val sleepFraction = (g1.sleep.durationMinutes.toFloat() / goals.sleepGoalMinutes).coerceAtMost(1f)
                score += (sleepFraction * 20).toInt()
            }
            // 4. Exercise progress (up to 20 pts)
            val exerciseMins = (totalExerciseSec / 60).toInt()
            if (goals.exerciseGoalMinutes > 0) {
                val exFraction = (exerciseMins.toFloat() / goals.exerciseGoalMinutes).coerceAtMost(1f)
                score += (exFraction * 20).toInt()
            }
            // 5. Habits completed (up to 20 pts)
            if (g2.habits.isNotEmpty()) {
                val habitFraction = (completedSet.size.toFloat() / goals.habitsGoalCount.coerceAtMost(g2.habits.size)).coerceAtMost(1f)
                score += (habitFraction * 20).toInt()
            }

            TodayWellnessSummary(
                date = today,
                totalSteps = totalSteps,
                totalWaterMl = totalWater,
                totalExerciseSeconds = totalExerciseSec,
                sleepEntry = g1.sleep,
                latestMood = g1.moods.firstOrNull(),
                activeHabits = g2.habits,
                completedHabitIds = completedSet,
                meals = g2.meals,
                activities = g1.activities,
                balanceScore = score.coerceIn(0, 100)
            )
        }
    }

    private data class CombinedGroup1(
        val activities: List<ActivityEntry>,
        val exercises: List<ExerciseSession>,
        val waters: List<WaterEntry>,
        val sleep: SleepEntry?,
        val moods: List<MoodEntry>
    )

    private data class CombinedGroup2(
        val meals: List<MealEntry>,
        val habits: List<Habit>,
        val completions: List<HabitCompletion>
    )

    fun getWeeklyInsights(goals: UserGoals): Flow<WeeklyInsights> {
        val last7Days = DateTimeUtils.getLast7Days()
        val startDate = last7Days.first()
        val endDate = last7Days.last()

        val actsFlow = activityDao.getActivitiesBetweenDates(startDate, endDate)
        val exersFlow = exerciseDao.getSessionsBetweenDates(startDate, endDate)
        val waterFlow = waterDao.getWaterBetweenDates(startDate, endDate)
        val sleepFlow = sleepDao.getSleepBetweenDates(startDate, endDate)
        val compsFlow = habitDao.getCompletionsBetweenDates(startDate, endDate)

        return combine(actsFlow, exersFlow, waterFlow, sleepFlow, compsFlow) { acts, exers, waters, sleeps, comps ->
            val actDates = acts.map { it.date }.toSet()
            val daysWithActivity = actDates.size

            val waterByDate = waters.groupBy { it.date }
            val daysWithWaterGoal = waterByDate.count { (_, entries) ->
                entries.sumOf { it.amountMl } >= goals.waterGoalMl
            }

            val daysWithSleep = sleeps.map { it.date }.toSet().size
            val totalExerciseMin = (exers.sumOf { it.durationSeconds } / 60).toInt()
            val totalHabitsDone = comps.size

            // Compute current streak of logging anything
            val allLoggedDates = (actDates + waterByDate.keys + sleeps.map { it.date } + comps.map { it.date }).toSet()
            var streak = 0
            for (i in 0..30) {
                val d = DateTimeUtils.getDaysAgoString(i)
                if (allLoggedDates.contains(d)) {
                    streak++
                } else if (i > 0) {
                    break
                }
            }

            WeeklyInsights(
                daysWithActivity = daysWithActivity,
                daysWithWaterGoal = daysWithWaterGoal,
                daysWithSleepLogged = daysWithSleep,
                totalExerciseMinutes = totalExerciseMin,
                totalHabitsCompleted = totalHabitsDone,
                currentStreakDays = streak,
                activityConsistencyText = "You logged activity on $daysWithActivity of the last 7 days.",
                exerciseTrendText = if (totalExerciseMin > 0) {
                    "Total $totalExerciseMin mins of exercise recorded over the past week."
                } else {
                    "No exercise sessions recorded yet this week."
                },
                sleepTrendText = if (daysWithSleep > 0) {
                    "Sleep logged on $daysWithSleep of 7 nights."
                } else {
                    "Record your sleep to track your rest routine."
                },
                waterTrendText = "Reached hydration target on $daysWithWaterGoal of 7 days.",
                habitTrendText = "$totalHabitsDone habit milestones completed this week."
            )
        }
    }

    // CRUD helpers
    suspend fun addActivity(entry: ActivityEntry): Long = activityDao.insert(entry)
    suspend fun deleteActivity(entry: ActivityEntry) = activityDao.delete(entry)
    fun getAllActivities(): Flow<List<ActivityEntry>> = activityDao.getAllActivities()

    suspend fun addExerciseSession(session: ExerciseSession): Long = exerciseDao.insert(session)
    suspend fun deleteExerciseSession(session: ExerciseSession) = exerciseDao.delete(session)
    fun getAllExerciseSessions(): Flow<List<ExerciseSession>> = exerciseDao.getAllSessions()

    suspend fun addWater(amountMl: Int, date: String = DateTimeUtils.todayString()) =
        waterDao.insert(WaterEntry(amountMl = amountMl, date = date))
    suspend fun deleteWater(entry: WaterEntry) = waterDao.delete(entry)
    fun getTodayWater(date: String = DateTimeUtils.todayString()): Flow<List<WaterEntry>> =
        waterDao.getWaterForDate(date)

    suspend fun saveSleep(entry: SleepEntry): Long = sleepDao.insert(entry)
    suspend fun deleteSleep(entry: SleepEntry) = sleepDao.delete(entry)
    fun getSleepForDate(date: String): Flow<SleepEntry?> = sleepDao.getSleepForDate(date)

    suspend fun addMood(entry: MoodEntry): Long = moodDao.insert(entry)
    suspend fun deleteMood(entry: MoodEntry) = moodDao.delete(entry)
    fun getAllMoods(): Flow<List<MoodEntry>> = moodDao.getAllMoods()

    suspend fun addMeal(entry: MealEntry): Long = mealDao.insert(entry)
    suspend fun deleteMeal(entry: MealEntry) = mealDao.delete(entry)
    fun getMealsForDate(date: String): Flow<List<MealEntry>> = mealDao.getMealsForDate(date)

    fun getActiveHabits(): Flow<List<Habit>> = habitDao.getActiveHabits()
    suspend fun addHabit(habit: Habit): Long = habitDao.insertHabit(habit)
    suspend fun deleteHabit(habit: Habit) {
        habitDao.deleteCompletionsForHabit(habit.id)
        habitDao.deleteHabit(habit)
    }

    suspend fun toggleHabitCompletion(habitId: Long, date: String, isCurrentlyCompleted: Boolean) {
        if (isCurrentlyCompleted) {
            habitDao.deleteCompletion(habitId, date)
        } else {
            habitDao.insertCompletion(HabitCompletion(habitId = habitId, date = date))
        }
    }

    fun getCompletionsForDate(date: String): Flow<List<HabitCompletion>> =
        habitDao.getCompletionsForDate(date)

    suspend fun deleteAllUserData() {
        activityDao.deleteAll()
        exerciseDao.deleteAll()
        waterDao.deleteAll()
        sleepDao.deleteAll()
        moodDao.deleteAll()
        mealDao.deleteAll()
        habitDao.deleteAllCompletions()
        habitDao.deleteAllHabits()
    }

    fun getDatabaseInstance(): VitaFlowDatabase = database
}
