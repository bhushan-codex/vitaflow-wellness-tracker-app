package com.example.ui.screens.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActivityEntry
import com.example.data.model.ExerciseSession
import com.example.ui.components.ActivityLogBottomSheet
import com.example.ui.theme.VitaError
import com.example.ui.theme.VitaPrimary
import com.example.ui.theme.VitaSecondary
import com.example.ui.theme.VitaSuccess
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityScreen(
    isSensorAvailable: Boolean,
    sensorSteps: Int,
    isExerciseActive: Boolean,
    isExercisePaused: Boolean,
    exerciseDurationSeconds: Long,
    currentExerciseType: String,
    allActivities: List<ActivityEntry>,
    allExercises: List<ExerciseSession>,
    onStartExercise: (String) -> Unit,
    onPauseExercise: () -> Unit,
    onResumeExercise: () -> Unit,
    onStopAndSaveExercise: (String) -> Unit,
    onCancelExercise: () -> Unit,
    onManualLogActivity: (type: String, durationMin: Int, steps: Int, distanceMeters: Int, calories: Int, notes: String) -> Unit,
    onDeleteExercise: (ExerciseSession) -> Unit,
    onDeleteActivity: (ActivityEntry) -> Unit
) {
    var showManualActivitySheet by remember { mutableStateOf(false) }
    var showCompletionDialog by remember { mutableStateOf(false) }
    var completionNotes by remember { mutableStateOf("") }
    var selectedExerciseType by remember { mutableStateOf("Walking") }

    val exerciseTypes = listOf("Walking", "Running", "Cycling", "Strength", "Stretching", "Yoga", "Other")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Activity & Exercise",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                actions = {
                    IconButton(
                        onClick = { showManualActivitySheet = true },
                        modifier = Modifier.testTag("action_manual_activity_log")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Manual Entry")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .testTag("activity_screen_list"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Live Exercise Tracker Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("exercise_session_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isExerciseActive) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (isExerciseActive) {
                            // Active Session In Progress
                            Text(
                                text = "SESSION IN PROGRESS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = VitaPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = currentExerciseType,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = DateTimeUtils.formatDuration(exerciseDurationSeconds),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 44.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isExercisePaused) {
                                    Button(
                                        onClick = onResumeExercise,
                                        modifier = Modifier.testTag("resume_exercise_btn"),
                                        colors = ButtonDefaults.buttonColors(containerColor = VitaSuccess)
                                    ) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Resume")
                                    }
                                } else {
                                    FilledTonalButton(
                                        onClick = onPauseExercise,
                                        modifier = Modifier.testTag("pause_exercise_btn")
                                    ) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Pause")
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Button(
                                    onClick = { showCompletionDialog = true },
                                    modifier = Modifier.testTag("finish_exercise_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = VitaPrimary)
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "Finish")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Finish")
                                }
                            }
                        } else {
                            // Not Active: Start Exercise Setup
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(VitaPrimary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Timer, contentDescription = null, tint = VitaPrimary)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Start Exercise Session",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "Track duration offline without battery drain",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(exerciseTypes) { type ->
                                    FilterChip(
                                        selected = selectedExerciseType == type,
                                        onClick = { selectedExerciseType = type },
                                        label = { Text(type) },
                                        modifier = Modifier.testTag("exercise_chip_$type")
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { onStartExercise(selectedExerciseType) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("start_exercise_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Start $selectedExerciseType")
                            }
                        }
                    }
                }
            }

            // 2. Device Step Sensor Status Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("step_sensor_status_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = if (isSensorAvailable) Icons.Default.DirectionsWalk else Icons.Default.SensorsOff,
                                contentDescription = null,
                                tint = if (isSensorAvailable) VitaSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Device Step Sensor",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isSensorAvailable) {
                            Text(
                                text = "Hardware step counter active",
                                style = MaterialTheme.typography.bodySmall,
                                color = VitaSuccess
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "$sensorSteps sensor steps detected today",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        } else {
                            Text(
                                text = "Step tracking isn't available on this device.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = { showManualActivitySheet = true },
                                modifier = Modifier.testTag("manual_activity_fallback_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Manual activity logging")
                            }
                        }
                    }
                }
            }

            // 3. Exercise Sessions Log Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Exercise Sessions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${allExercises.size} sessions",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allExercises.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No exercise sessions recorded yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Tap 'Start' above to time your walk, run, or yoga.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(allExercises) { session ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = session.type,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${DateTimeUtils.formatDuration(session.durationSeconds)} • ${DateTimeUtils.formatDisplayDate(session.date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (session.notes.isNotBlank()) {
                                    Text(
                                        text = "\"${session.notes}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteExercise(session) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // 4. Manual Activity Entries
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Logged Activities",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${allActivities.size} logged",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (allActivities.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No manual activity entries yet.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            FilledTonalButton(
                                onClick = { showManualActivitySheet = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Log an activity")
                            }
                        }
                    }
                }
            } else {
                items(allActivities) { entry ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = entry.type,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "${entry.durationMinutes} mins${if (entry.steps > 0) " • ${entry.steps} steps" else ""} • ${DateTimeUtils.formatDisplayDate(entry.date)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (entry.notes.isNotBlank()) {
                                    Text(
                                        text = "\"${entry.notes}\"",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            IconButton(onClick = { onDeleteActivity(entry) }) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Workout Complete Dialog
    if (showCompletionDialog) {
        AlertDialog(
            onDismissRequest = { showCompletionDialog = false },
            title = {
                Text("Workout Complete", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold))
            },
            text = {
                Column {
                    Text("Type: $currentExerciseType")
                    Text("Duration: ${DateTimeUtils.formatDuration(exerciseDurationSeconds)}")
                    Text("Date: ${DateTimeUtils.formatDisplayDate(DateTimeUtils.todayString())}")
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = completionNotes,
                        onValueChange = { completionNotes = it },
                        label = { Text("Optional notes") },
                        placeholder = { Text("e.g. Felt relaxed, steady pace") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStopAndSaveExercise(completionNotes)
                        completionNotes = ""
                        showCompletionDialog = false
                    },
                    modifier = Modifier.testTag("save_workout_confirm_btn")
                ) {
                    Text("Save Workout")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        onCancelExercise()
                        showCompletionDialog = false
                    }
                ) {
                    Text("Discard", color = VitaError)
                }
            }
        )
    }

    if (showManualActivitySheet) {
        ActivityLogBottomSheet(
            onDismiss = { showManualActivitySheet = false },
            onSaveActivity = onManualLogActivity
        )
    }
}
