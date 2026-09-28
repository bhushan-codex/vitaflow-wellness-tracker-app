package com.example.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.preferences.UserGoals
import com.example.data.repository.TodayWellnessSummary
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    userName: String,
    todaySummary: TodayWellnessSummary,
    goals: UserGoals,
    onAddWater: (Int) -> Unit,
    onSaveActivity: (type: String, durationMin: Int, steps: Int, distanceMeters: Int, calories: Int, notes: String) -> Unit,
    onSaveSleep: (bedHour: Int, bedMin: Int, wakeHour: Int, wakeMin: Int, quality: String, notes: String) -> Unit,
    onSaveMood: (mood: String, note: String) -> Unit,
    onSaveMeal: (mealType: String, foodName: String, note: String, timeStr: String) -> Unit,
    onToggleHabit: (habitId: Long, isDone: Boolean) -> Unit,
    onNavigateToActivityTab: () -> Unit,
    onNavigateToHabitsTab: () -> Unit
) {
    var showWaterSheet by remember { mutableStateOf(false) }
    var showActivitySheet by remember { mutableStateOf(false) }
    var showSleepSheet by remember { mutableStateOf(false) }
    var showMoodSheet by remember { mutableStateOf(false) }
    var showMealSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "${DateTimeUtils.getGreeting()}, $userName",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Your wellness today",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("home_screen_list"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Primary Visual: Wellness Ring Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("wellness_balance_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        WellnessRing(score = todaySummary.balanceScore)

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Configurable habit indicator • Non-medical",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // 2. Quick Actions Horizontal Bar
            item {
                Column {
                    Text(
                        text = "Quick Log",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .testTag("quick_actions_row"),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.WaterDrop,
                            label = "+ Water",
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            onClick = { showWaterSheet = true },
                            tag = "quick_action_water"
                        )
                        QuickActionButton(
                            icon = Icons.Default.DirectionsRun,
                            label = "+ Activity",
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            onClick = { showActivitySheet = true },
                            tag = "quick_action_activity"
                        )
                        QuickActionButton(
                            icon = Icons.Default.Timer,
                            label = "+ Exercise",
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            onClick = onNavigateToActivityTab,
                            tag = "quick_action_exercise"
                        )
                        QuickActionButton(
                            icon = Icons.Default.Bedtime,
                            label = "+ Sleep",
                            containerColor = CardSleep.copy(alpha = 0.15f),
                            contentColor = CardSleep,
                            onClick = { showSleepSheet = true },
                            tag = "quick_action_sleep"
                        )
                        QuickActionButton(
                            icon = Icons.Default.SentimentSatisfied,
                            label = "+ Mood",
                            containerColor = CardMood.copy(alpha = 0.15f),
                            contentColor = CardMood,
                            onClick = { showMoodSheet = true },
                            tag = "quick_action_mood"
                        )
                        QuickActionButton(
                            icon = Icons.Default.Restaurant,
                            label = "+ Meal",
                            containerColor = CardMeal.copy(alpha = 0.15f),
                            contentColor = CardMeal,
                            onClick = { showMealSheet = true },
                            tag = "quick_action_meal"
                        )
                    }
                }
            }

            // 3. Water Hydration Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_water_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(VitaSecondary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WaterDrop,
                                        contentDescription = "Hydration",
                                        tint = VitaSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Hydration",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            IconButton(
                                onClick = { onAddWater(250) },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(VitaSecondary.copy(alpha = 0.1f))
                                    .testTag("quick_add_250_btn")
                            ) {
                                Text(
                                    "+250",
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                    color = VitaSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${todaySummary.totalWaterMl} ml",
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = "Target: ${goals.waterGoalMl} ml",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val waterProgress = if (goals.waterGoalMl > 0) {
                            (todaySummary.totalWaterMl.toFloat() / goals.waterGoalMl).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { waterProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = VitaSecondary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // 4. Activity & Steps Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_activity_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(VitaPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsWalk,
                                        contentDescription = "Activity",
                                        tint = VitaPrimary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Activity & Movement",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            TextButton(onClick = onNavigateToActivityTab) {
                                Text("View all")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${todaySummary.totalSteps}",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Steps today",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                val exerciseMins = (todaySummary.totalExerciseSeconds / 60).toInt()
                                Text(
                                    text = "$exerciseMins mins",
                                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Active exercise",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val stepProgress = if (goals.stepGoal > 0) {
                            (todaySummary.totalSteps.toFloat() / goals.stepGoal).coerceIn(0f, 1f)
                        } else 0f

                        LinearProgressIndicator(
                            progress = { stepProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = VitaPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            // 5. Sleep Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_sleep_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CardSleep.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Bedtime,
                                        contentDescription = "Sleep",
                                        tint = CardSleep,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Sleep Log",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            if (todaySummary.sleepEntry == null) {
                                TextButton(onClick = { showSleepSheet = true }) {
                                    Text("Add sleep")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (todaySummary.sleepEntry != null) {
                            val sleep = todaySummary.sleepEntry
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = DateTimeUtils.formatMinutesToHours(sleep.durationMinutes),
                                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "${String.format("%02d:%02d", sleep.bedtimeHour, sleep.bedtimeMinute)} - ${String.format("%02d:%02d", sleep.wakeHour, sleep.wakeMinute)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AssistChip(
                                    onClick = {},
                                    label = { Text(sleep.quality) },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = CardSleep.copy(alpha = 0.1f)
                                    )
                                )
                            }
                            if (sleep.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "\"${sleep.notes}\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        } else {
                            Text(
                                text = "No sleep record for today yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 6. Today's Habits Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_habits_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(CardHabit.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Habits",
                                        tint = CardHabit,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Daily Habits",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            TextButton(onClick = onNavigateToHabitsTab) {
                                Text("Manage")
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (todaySummary.activeHabits.isEmpty()) {
                            Text(
                                text = "No habits created yet. Tap Manage to add habits.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            todaySummary.activeHabits.take(4).forEach { habit ->
                                val isDone = todaySummary.completedHabitIds.contains(habit.id)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = isDone,
                                        onCheckedChange = { onToggleHabit(habit.id, isDone) },
                                        modifier = Modifier.testTag("home_habit_toggle_${habit.id}")
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = if (isDone) FontWeight.Normal else FontWeight.Medium
                                        ),
                                        color = if (isDone) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. Mood Check-in Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("home_mood_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Today's Mood",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                            TextButton(onClick = { showMoodSheet = true }) {
                                Text(if (todaySummary.latestMood == null) "Log mood" else "Update")
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        if (todaySummary.latestMood != null) {
                            val mood = todaySummary.latestMood
                            val emoji = when (mood.mood) {
                                "Great" -> "😊"
                                "Good" -> "🙂"
                                "Okay" -> "😐"
                                "Low" -> "😕"
                                "Tired" -> "😴"
                                else -> "🙂"
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(emoji, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = mood.mood,
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    if (mood.note.isNotBlank()) {
                                        Text(
                                            text = mood.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = "Take a moment to record how you are feeling today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 8. General Wellness Disclaimer
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Information",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "VitaFlow is an offline personal wellness journal for lifestyle habit tracking. It does not provide medical advice or diagnoses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    // Modal Bottom Sheets
    if (showWaterSheet) {
        WaterLogBottomSheet(
            onDismiss = { showWaterSheet = false },
            onLogWater = onAddWater
        )
    }

    if (showActivitySheet) {
        ActivityLogBottomSheet(
            onDismiss = { showActivitySheet = false },
            onSaveActivity = onSaveActivity
        )
    }

    if (showSleepSheet) {
        SleepLogBottomSheet(
            onDismiss = { showSleepSheet = false },
            onSaveSleep = onSaveSleep
        )
    }

    if (showMoodSheet) {
        MoodLogBottomSheet(
            onDismiss = { showMoodSheet = false },
            onSaveMood = onSaveMood
        )
    }

    if (showMealSheet) {
        MealLogBottomSheet(
            onDismiss = { showMealSheet = false },
            onSaveMeal = onSaveMeal
        )
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit,
    tag: String
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = Modifier.testTag(tag)
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = label, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
    }
}
