package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.backup.BackupManager
import com.example.data.backup.ImportResult
import com.example.data.model.*
import com.example.data.preferences.UserGoals
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.repository.TodayWellnessSummary
import com.example.data.repository.WeeklyInsights
import com.example.data.repository.WellnessRepository
import com.example.data.sensor.StepSensorManager
import com.example.util.DateTimeUtils
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: WellnessRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val stepSensorManager: StepSensorManager
) : ViewModel() {

    val isOnboardingCompleted: StateFlow<Boolean> = preferencesRepository.isOnboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    val userName: StateFlow<String> = preferencesRepository.userName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "Friend")

    val userGoals: StateFlow<UserGoals> = preferencesRepository.userGoals
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserGoals())

    val themeMode: StateFlow<String> = preferencesRepository.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val isSensorAvailable: StateFlow<Boolean> = stepSensorManager.isSensorAvailable
    val sensorSteps: StateFlow<Int> = stepSensorManager.sensorSteps

    // Dynamic today's summary reacting to goal changes
    val todaySummary: StateFlow<TodayWellnessSummary> = userGoals.flatMapLatest { goals ->
        repository.getTodaySummary(goals)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        TodayWellnessSummary(
            date = DateTimeUtils.todayString(),
            totalSteps = 0,
            totalWaterMl = 0,
            totalExerciseSeconds = 0,
            sleepEntry = null,
            latestMood = null,
            activeHabits = emptyList(),
            completedHabitIds = emptySet(),
            meals = emptyList(),
            activities = emptyList(),
            balanceScore = 0
        )
    )

    val weeklyInsights: StateFlow<WeeklyInsights> = userGoals.flatMapLatest { goals ->
        repository.getWeeklyInsights(goals)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        WeeklyInsights(
            daysWithActivity = 0,
            daysWithWaterGoal = 0,
            daysWithSleepLogged = 0,
            totalExerciseMinutes = 0,
            totalHabitsCompleted = 0,
            currentStreakDays = 0,
            activityConsistencyText = "Start logging to see your weekly trends.",
            exerciseTrendText = "No exercise sessions recorded yet this week.",
            sleepTrendText = "Track your sleep to see consistency.",
            waterTrendText = "Stay hydrated and log your daily water.",
            habitTrendText = "Build daily habits one day at a time."
        )
    )

    val allActivities: StateFlow<List<ActivityEntry>> = repository.getAllActivities()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allExercises: StateFlow<List<ExerciseSession>> = repository.getAllExerciseSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMoods: StateFlow<List<MoodEntry>> = repository.getAllMoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Exercise Session Tracking
    private val _isExerciseActive = MutableStateFlow(false)
    val isExerciseActive: StateFlow<Boolean> = _isExerciseActive.asStateFlow()

    private val _isExercisePaused = MutableStateFlow(false)
    val isExercisePaused: StateFlow<Boolean> = _isExercisePaused.asStateFlow()

    private val _exerciseDurationSeconds = MutableStateFlow(0L)
    val exerciseDurationSeconds: StateFlow<Long> = _exerciseDurationSeconds.asStateFlow()

    private val _currentExerciseType = MutableStateFlow("Walking")
    val currentExerciseType: StateFlow<String> = _currentExerciseType.asStateFlow()

    private var timerJob: Job? = null

    init {
        stepSensorManager.startListening()
    }

    override fun onCleared() {
        super.onCleared()
        stepSensorManager.stopListening()
        timerJob?.cancel()
    }

    // Exercise timer functions
    fun startExercise(type: String) {
        _currentExerciseType.value = type
        _exerciseDurationSeconds.value = 0L
        _isExerciseActive.value = true
        _isExercisePaused.value = false

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_isExerciseActive.value) {
                delay(1000)
                if (!_isExercisePaused.value) {
                    _exerciseDurationSeconds.value += 1
                }
            }
        }
    }

    fun pauseExercise() {
        _isExercisePaused.value = true
    }

    fun resumeExercise() {
        _isExercisePaused.value = false
    }

    fun stopAndSaveExercise(notes: String = "") {
        val duration = _exerciseDurationSeconds.value
        val type = _currentExerciseType.value
        _isExerciseActive.value = false
        _isExercisePaused.value = false
        timerJob?.cancel()

        if (duration >= 5) {
            viewModelScope.launch {
                repository.addExerciseSession(
                    ExerciseSession(
                        type = type,
                        durationSeconds = duration,
                        notes = notes.trim(),
                        date = DateTimeUtils.todayString()
                    )
                )
            }
        }
        _exerciseDurationSeconds.value = 0L
    }

    fun cancelExercise() {
        _isExerciseActive.value = false
        _isExercisePaused.value = false
        _exerciseDurationSeconds.value = 0L
        timerJob?.cancel()
    }

    // Quick Action Logging
    fun addWater(amountMl: Int) {
        viewModelScope.launch {
            repository.addWater(amountMl)
        }
    }

    fun addActivity(type: String, durationMinutes: Int, steps: Int, distanceMeters: Int, calories: Int, notes: String) {
        viewModelScope.launch {
            repository.addActivity(
                ActivityEntry(
                    type = type,
                    durationMinutes = durationMinutes,
                    steps = steps,
                    distanceMeters = distanceMeters,
                    caloriesEstimate = calories,
                    notes = notes.trim(),
                    date = DateTimeUtils.todayString()
                )
            )
        }
    }

    fun deleteActivity(entry: ActivityEntry) {
        viewModelScope.launch { repository.deleteActivity(entry) }
    }

    fun deleteExercise(session: ExerciseSession) {
        viewModelScope.launch { repository.deleteExerciseSession(session) }
    }

    fun saveSleep(bedHour: Int, bedMinute: Int, wakeHour: Int, wakeMinute: Int, quality: String, notes: String) {
        viewModelScope.launch {
            val duration = DateTimeUtils.calculateSleepDurationMinutes(bedHour, bedMinute, wakeHour, wakeMinute)
            repository.saveSleep(
                SleepEntry(
                    bedtimeHour = bedHour,
                    bedtimeMinute = bedMinute,
                    wakeHour = wakeHour,
                    wakeMinute = wakeMinute,
                    durationMinutes = duration,
                    quality = quality,
                    notes = notes.trim(),
                    date = DateTimeUtils.todayString()
                )
            )
        }
    }

    fun addMood(mood: String, note: String) {
        viewModelScope.launch {
            repository.addMood(
                MoodEntry(
                    mood = mood,
                    note = note.trim(),
                    date = DateTimeUtils.todayString()
                )
            )
        }
    }

    fun deleteMood(entry: MoodEntry) {
        viewModelScope.launch { repository.deleteMood(entry) }
    }

    fun addMeal(mealType: String, foodName: String, note: String, timeStr: String) {
        viewModelScope.launch {
            repository.addMeal(
                MealEntry(
                    mealType = mealType,
                    foodName = foodName.trim(),
                    note = note.trim(),
                    timeStr = timeStr,
                    date = DateTimeUtils.todayString()
                )
            )
        }
    }

    fun deleteMeal(entry: MealEntry) {
        viewModelScope.launch { repository.deleteMeal(entry) }
    }

    fun toggleHabit(habitId: Long, date: String, isCurrentlyCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitCompletion(habitId, date, isCurrentlyCompleted)
        }
    }

    fun addNewHabit(name: String, category: String, targetWeekly: Int) {
        viewModelScope.launch {
            repository.addHabit(
                Habit(
                    name = name.trim(),
                    category = category,
                    targetPerWeek = targetWeekly
                )
            )
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun updateGoals(stepGoal: Int, waterGoalMl: Int, sleepHours: Float, exerciseMins: Int, habitsCount: Int) {
        viewModelScope.launch {
            preferencesRepository.updateGoals(stepGoal, waterGoalMl, sleepHours, exerciseMins, habitsCount)
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    fun completeOnboarding(name: String, stepGoal: Int, waterGoalMl: Int, sleepHours: Float) {
        viewModelScope.launch {
            preferencesRepository.setUserName(name)
            preferencesRepository.updateGoals(
                stepGoal = stepGoal,
                waterGoalMl = waterGoalMl,
                sleepHours = sleepHours,
                exerciseMins = 30,
                habitCount = 3
            )
            preferencesRepository.setOnboardingCompleted(true)
        }
    }

    suspend fun exportJson(): String {
        return BackupManager.exportToJson(repository.getDatabaseInstance())
    }

    fun importJson(json: String, onComplete: (ImportResult) -> Unit) {
        viewModelScope.launch {
            val result = BackupManager.importFromJson(json, repository.getDatabaseInstance())
            onComplete(result)
        }
    }

    fun deleteAllData(onComplete: () -> Unit) {
        viewModelScope.launch {
            repository.deleteAllUserData()
            onComplete()
        }
    }
}

class MainViewModelFactory(
    private val repository: WellnessRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val stepSensorManager: StepSensorManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, preferencesRepository, stepSensorManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
