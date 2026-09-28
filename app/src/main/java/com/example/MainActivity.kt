package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainViewModel
import com.example.ui.MainViewModelFactory
import com.example.ui.screens.activity.ActivityScreen
import com.example.ui.screens.habits.HabitsScreen
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.insights.InsightsScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.profile.ProfileScreen
import com.example.ui.theme.VitaFlowTheme

enum class VitaTab(val label: String, val tag: String) {
    HOME("Home", "nav_home"),
    ACTIVITY("Activity", "nav_activity"),
    INSIGHTS("Insights", "nav_insights"),
    HABITS("Habits", "nav_habits"),
    PROFILE("Profile", "nav_profile")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as VitaFlowApp
        val viewModel: MainViewModel by viewModels {
            MainViewModelFactory(
                repository = app.repository,
                preferencesRepository = app.preferencesRepository,
                stepSensorManager = app.stepSensorManager
            )
        }

        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val isDarkTheme = when (themeMode) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }

            VitaFlowTheme(darkTheme = isDarkTheme) {
                val isOnboardingCompleted by viewModel.isOnboardingCompleted.collectAsStateWithLifecycle()

                if (!isOnboardingCompleted) {
                    OnboardingScreen(
                        onFinishOnboarding = { name, steps, water, sleep ->
                            viewModel.completeOnboarding(name, steps, water, sleep)
                        }
                    )
                } else {
                    VitaFlowMainScaffold(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun VitaFlowMainScaffold(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(VitaTab.HOME) }

    val userName by viewModel.userName.collectAsStateWithLifecycle()
    val todaySummary by viewModel.todaySummary.collectAsStateWithLifecycle()
    val goals by viewModel.userGoals.collectAsStateWithLifecycle()
    val weeklyInsights by viewModel.weeklyInsights.collectAsStateWithLifecycle()
    val allActivities by viewModel.allActivities.collectAsStateWithLifecycle()
    val allExercises by viewModel.allExercises.collectAsStateWithLifecycle()
    val allMoods by viewModel.allMoods.collectAsStateWithLifecycle()
    val isSensorAvailable by viewModel.isSensorAvailable.collectAsStateWithLifecycle()
    val sensorSteps by viewModel.sensorSteps.collectAsStateWithLifecycle()
    val isExerciseActive by viewModel.isExerciseActive.collectAsStateWithLifecycle()
    val isExercisePaused by viewModel.isExercisePaused.collectAsStateWithLifecycle()
    val exerciseDuration by viewModel.exerciseDurationSeconds.collectAsStateWithLifecycle()
    val currentExerciseType by viewModel.currentExerciseType.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()

    BackHandler(enabled = selectedTab != VitaTab.HOME) {
        selectedTab = VitaTab.HOME
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                VitaTab.values().forEach { tab ->
                    val isSelected = selectedTab == tab
                    val (selectedIcon, unselectedIcon) = when (tab) {
                        VitaTab.HOME -> Icons.Filled.Home to Icons.Outlined.Home
                        VitaTab.ACTIVITY -> Icons.Filled.DirectionsRun to Icons.Outlined.DirectionsRun
                        VitaTab.INSIGHTS -> Icons.Filled.Insights to Icons.Outlined.Insights
                        VitaTab.HABITS -> Icons.Filled.CheckCircle to Icons.Outlined.CheckCircle
                        VitaTab.PROFILE -> Icons.Filled.Person to Icons.Outlined.Person
                    }

                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) selectedIcon else unselectedIcon,
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) },
                        modifier = Modifier.testTag(tab.tag)
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectedTab) {
                VitaTab.HOME -> {
                    HomeScreen(
                        userName = userName,
                        todaySummary = todaySummary,
                        goals = goals,
                        onAddWater = { ml -> viewModel.addWater(ml) },
                        onSaveActivity = { type, min, steps, dist, cal, notes ->
                            viewModel.addActivity(type, min, steps, dist, cal, notes)
                        },
                        onSaveSleep = { bedH, bedM, wakeH, wakeM, q, notes ->
                            viewModel.saveSleep(bedH, bedM, wakeH, wakeM, q, notes)
                        },
                        onSaveMood = { mood, note -> viewModel.addMood(mood, note) },
                        onSaveMeal = { type, name, note, time -> viewModel.addMeal(type, name, note, time) },
                        onToggleHabit = { habitId, isDone ->
                            viewModel.toggleHabit(habitId, todaySummary.date, isDone)
                        },
                        onNavigateToActivityTab = { selectedTab = VitaTab.ACTIVITY },
                        onNavigateToHabitsTab = { selectedTab = VitaTab.HABITS }
                    )
                }
                VitaTab.ACTIVITY -> {
                    ActivityScreen(
                        isSensorAvailable = isSensorAvailable,
                        sensorSteps = sensorSteps,
                        isExerciseActive = isExerciseActive,
                        isExercisePaused = isExercisePaused,
                        exerciseDurationSeconds = exerciseDuration,
                        currentExerciseType = currentExerciseType,
                        allActivities = allActivities,
                        allExercises = allExercises,
                        onStartExercise = { type -> viewModel.startExercise(type) },
                        onPauseExercise = { viewModel.pauseExercise() },
                        onResumeExercise = { viewModel.resumeExercise() },
                        onStopAndSaveExercise = { notes -> viewModel.stopAndSaveExercise(notes) },
                        onCancelExercise = { viewModel.cancelExercise() },
                        onManualLogActivity = { type, min, steps, dist, cal, notes ->
                            viewModel.addActivity(type, min, steps, dist, cal, notes)
                        },
                        onDeleteExercise = { session -> viewModel.deleteExercise(session) },
                        onDeleteActivity = { entry -> viewModel.deleteActivity(entry) }
                    )
                }
                VitaTab.INSIGHTS -> {
                    InsightsScreen(
                        insights = weeklyInsights,
                        allActivities = allActivities,
                        allExercises = allExercises,
                        allMoods = allMoods
                    )
                }
                VitaTab.HABITS -> {
                    HabitsScreen(
                        activeHabits = todaySummary.activeHabits,
                        completedHabitIds = todaySummary.completedHabitIds,
                        onToggleHabit = { habitId, isDone ->
                            viewModel.toggleHabit(habitId, todaySummary.date, isDone)
                        },
                        onAddNewHabit = { name, category, weeklyTarget ->
                            viewModel.addNewHabit(name, category, weeklyTarget)
                        },
                        onDeleteHabit = { habit -> viewModel.deleteHabit(habit) }
                    )
                }
                VitaTab.PROFILE -> {
                    ProfileScreen(
                        userName = userName,
                        goals = goals,
                        themeMode = themeMode,
                        onUpdateGoals = { steps, water, sleep, exercise, habits ->
                            viewModel.updateGoals(steps, water, sleep, exercise, habits)
                        },
                        onSetThemeMode = { mode -> viewModel.setThemeMode(mode) },
                        onExportData = { viewModel.exportJson() },
                        onImportData = { json, cb -> viewModel.importJson(json, cb) },
                        onDeleteAllData = { cb -> viewModel.deleteAllData(cb) }
                    )
                }
            }
        }
    }
}
