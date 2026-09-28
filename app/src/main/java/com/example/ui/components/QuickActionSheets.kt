package com.example.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.VitaPrimary
import com.example.util.DateTimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterLogBottomSheet(
    onDismiss: () -> Unit,
    onLogWater: (Int) -> Unit
) {
    var customAmount by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .testTag("water_bottom_sheet")
        ) {
            Text(
                text = "Log Water",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Quickly add to your daily hydration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Preset Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(250, 500, 750, 1000).forEach { ml ->
                    OutlinedButton(
                        onClick = {
                            onLogWater(ml)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("preset_water_${ml}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+$ml ml", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = customAmount,
                onValueChange = { customAmount = it.filter { char -> char.isDigit() } },
                label = { Text("Custom amount (ml)") },
                placeholder = { Text("e.g. 350") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_water_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    val amount = customAmount.toIntOrNull() ?: 250
                    if (amount > 0) {
                        onLogWater(amount)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_water_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = customAmount.isNotBlank()
            ) {
                Text("Add Custom Water")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActivityLogBottomSheet(
    onDismiss: () -> Unit,
    onSaveActivity: (type: String, durationMin: Int, steps: Int, distanceMeters: Int, calories: Int, notes: String) -> Unit
) {
    val activityTypes = listOf("Walking", "Running", "Cycling", "Workout", "Other")
    var selectedType by remember { mutableStateOf("Walking") }
    var durationMinutes by remember { mutableStateOf("30") }
    var steps by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .testTag("activity_bottom_sheet")
        ) {
            Text(
                text = "Log Activity",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Record an activity or manual movement session",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Activity Type", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                activityTypes.forEach { type ->
                    val isSelected = type == selectedType
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedType = type },
                        label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chip_activity_$type")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = durationMinutes,
                onValueChange = { durationMinutes = it.filter { char -> char.isDigit() } },
                label = { Text("Duration (minutes)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("duration_minutes_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = steps,
                onValueChange = { steps = it.filter { char -> char.isDigit() } },
                label = { Text("Estimated steps (optional)") },
                placeholder = { Text("e.g. 2500") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth().testTag("steps_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("e.g. Brisk neighborhood walk") },
                modifier = Modifier.fillMaxWidth().testTag("notes_input"),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val duration = durationMinutes.toIntOrNull() ?: 15
                    val stepCount = steps.toIntOrNull() ?: 0
                    onSaveActivity(selectedType, duration, stepCount, 0, 0, notes)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_activity_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Activity Entry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepLogBottomSheet(
    onDismiss: () -> Unit,
    onSaveSleep: (bedHour: Int, bedMin: Int, wakeHour: Int, wakeMin: Int, quality: String, notes: String) -> Unit
) {
    var bedtimeHour by remember { mutableIntStateOf(23) }
    var bedtimeMinute by remember { mutableIntStateOf(0) }
    var wakeHour by remember { mutableIntStateOf(7) }
    var wakeMinute by remember { mutableIntStateOf(0) }
    val qualities = listOf("Excellent", "Good", "Okay", "Poor")
    var selectedQuality by remember { mutableStateOf("Good") }
    var notes by remember { mutableStateOf("") }

    val calculatedDurationMinutes = remember(bedtimeHour, bedtimeMinute, wakeHour, wakeMinute) {
        DateTimeUtils.calculateSleepDurationMinutes(bedtimeHour, bedtimeMinute, wakeHour, wakeMinute)
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
                .testTag("sleep_bottom_sheet")
        ) {
            Text(
                text = "Log Sleep",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Manual sleep entry with overnight midnight calculation",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Calculated Duration Highlight Card
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Calculated Duration", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = DateTimeUtils.formatMinutesToHours(calculatedDurationMinutes),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = VitaPrimary
                        )
                    }
                    Text(
                        text = "${String.format("%02d:%02d", bedtimeHour, bedtimeMinute)} → ${String.format("%02d:%02d", wakeHour, wakeMinute)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Bedtime and Wake time pickers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Bedtime", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = bedtimeHour.toString(),
                            onValueChange = { bedtimeHour = (it.toIntOrNull() ?: 23).coerceIn(0, 23) },
                            label = { Text("Hour") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = bedtimeMinute.toString(),
                            onValueChange = { bedtimeMinute = (it.toIntOrNull() ?: 0).coerceIn(0, 59) },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Wake Time", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = wakeHour.toString(),
                            onValueChange = { wakeHour = (it.toIntOrNull() ?: 7).coerceIn(0, 23) },
                            label = { Text("Hour") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = wakeMinute.toString(),
                            onValueChange = { wakeMinute = (it.toIntOrNull() ?: 0).coerceIn(0, 59) },
                            label = { Text("Min") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Sleep Quality", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                qualities.forEach { q ->
                    FilterChip(
                        selected = selectedQuality == q,
                        onClick = { selectedQuality = q },
                        label = { Text(q, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("e.g. Slept peacefully without waking") },
                modifier = Modifier.fillMaxWidth().testTag("sleep_notes_input"),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onSaveSleep(bedtimeHour, bedtimeMinute, wakeHour, wakeMinute, selectedQuality, notes)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_sleep_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Sleep Record")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoodLogBottomSheet(
    onDismiss: () -> Unit,
    onSaveMood: (mood: String, note: String) -> Unit
) {
    val moods = listOf(
        "Great" to "😊",
        "Good" to "🙂",
        "Okay" to "😐",
        "Low" to "😕",
        "Tired" to "😴"
    )
    var selectedMood by remember { mutableStateOf("Good") }
    var note by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .testTag("mood_bottom_sheet")
        ) {
            Text(
                text = "Log Today's Mood",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Your recent mood entries (personal wellness awareness)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                moods.forEach { (name, emoji) ->
                    val isSelected = selectedMood == name
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 4.dp)
                            .clickable { selectedMood = name }
                            .testTag("mood_option_$name"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                        ) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                name,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Reflection note (optional)") },
                placeholder = { Text("What made you feel this way?") },
                modifier = Modifier.fillMaxWidth().testTag("mood_note_input"),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    onSaveMood(selectedMood, note)
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_mood_button"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Mood Entry")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealLogBottomSheet(
    onDismiss: () -> Unit,
    onSaveMeal: (mealType: String, foodName: String, note: String, timeStr: String) -> Unit
) {
    val mealTypes = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    var selectedMealType by remember { mutableStateOf("Lunch") }
    var foodName by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .testTag("meal_bottom_sheet")
        ) {
            Text(
                text = "Log Meal Note",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Journaling your daily food choices for mindful awareness",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                mealTypes.forEach { type ->
                    FilterChip(
                        selected = selectedMealType == type,
                        onClick = { selectedMealType = type },
                        label = { Text(type, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("chip_meal_$type")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = foodName,
                onValueChange = { foodName = it },
                label = { Text("What did you enjoy?") },
                placeholder = { Text("e.g. Oatmeal with fresh berries and almonds") },
                modifier = Modifier.fillMaxWidth().testTag("meal_food_input"),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Optional note") },
                placeholder = { Text("e.g. Felt energized and satisfied") },
                modifier = Modifier.fillMaxWidth().testTag("meal_note_input"),
                maxLines = 2,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    if (foodName.isNotBlank()) {
                        val currentTime = DateTimeUtils.formatTime(System.currentTimeMillis())
                        onSaveMeal(selectedMealType, foodName, note, currentTime)
                        onDismiss()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("save_meal_button"),
                shape = RoundedCornerShape(12.dp),
                enabled = foodName.isNotBlank()
            ) {
                Text("Save Meal Note")
            }
        }
    }
}
