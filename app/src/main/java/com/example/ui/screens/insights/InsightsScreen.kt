package com.example.ui.screens.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.repository.WeeklyInsights
import com.example.ui.theme.*
import com.example.util.DateTimeUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    insights: WeeklyInsights,
    allActivities: List<ActivityEntry>,
    allExercises: List<ExerciseSession>,
    allMoods: List<MoodEntry>
) {
    var selectedCalendarDate by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Trends & Calendar",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                    )
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
                .testTag("insights_screen_list"),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Weekly Consistency Streak Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("streak_banner_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .background(VitaPrimary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🔥", fontSize = 26.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "${insights.currentStreakDays} Day Logging Streak",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "Consistency over perfection. Keep building daily awareness.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // 2. Weekly Observations & Habits (Calibrated neutral observations)
            item {
                Text(
                    text = "Weekly Trends",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        InsightItemRow(
                            icon = Icons.Default.DirectionsWalk,
                            iconColor = VitaPrimary,
                            text = insights.activityConsistencyText
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        InsightItemRow(
                            icon = Icons.Default.Timer,
                            iconColor = VitaSecondary,
                            text = insights.exerciseTrendText
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        InsightItemRow(
                            icon = Icons.Default.WaterDrop,
                            iconColor = CardWater,
                            text = insights.waterTrendText
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        InsightItemRow(
                            icon = Icons.Default.Bedtime,
                            iconColor = CardSleep,
                            text = insights.sleepTrendText
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        InsightItemRow(
                            icon = Icons.Default.CheckCircle,
                            iconColor = VitaSuccess,
                            text = insights.habitTrendText
                        )
                    }
                }
            }

            // 3. Monthly Calendar with Activity Dots
            item {
                Text(
                    text = "Monthly Journal Calendar",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            item {
                MonthlyCalendarCard(
                    onSelectDate = { dateStr -> selectedCalendarDate = dateStr },
                    loggedActivityDates = allActivities.map { it.date }.toSet() + allExercises.map { it.date }.toSet()
                )
            }

            // 4. Mood Reflections History
            if (allMoods.isNotEmpty()) {
                item {
                    Text(
                        text = "Recent Mood Check-ins",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            allMoods.take(5).forEach { mood ->
                                val emoji = when (mood.mood) {
                                    "Great" -> "😊"
                                    "Good" -> "🙂"
                                    "Okay" -> "😐"
                                    "Low" -> "😕"
                                    "Tired" -> "😴"
                                    else -> "🙂"
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(emoji, fontSize = 22.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                mood.mood,
                                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                                            )
                                            if (mood.note.isNotBlank()) {
                                                Text(
                                                    mood.note,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        DateTimeUtils.formatDisplayDate(mood.date),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Selected Date Details Dialog
    if (selectedCalendarDate != null) {
        val date = selectedCalendarDate!!
        val dateActivities = allActivities.filter { it.date == date }
        val dateExercises = allExercises.filter { it.date == date }
        val dateMoods = allMoods.filter { it.date == date }

        AlertDialog(
            onDismissRequest = { selectedCalendarDate = null },
            title = {
                Text(
                    text = DateTimeUtils.formatDisplayDate(date),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (dateActivities.isEmpty() && dateExercises.isEmpty() && dateMoods.isEmpty()) {
                        Text(
                            "No activities or notes recorded on this day.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        if (dateActivities.isNotEmpty()) {
                            Text(
                                "Activities (${dateActivities.size}):",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            dateActivities.forEach {
                                Text("• ${it.type} (${it.durationMinutes}m${if (it.steps > 0) ", ${it.steps} steps" else ""})")
                            }
                        }
                        if (dateExercises.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Exercises (${dateExercises.size}):",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            dateExercises.forEach {
                                Text("• ${it.type} (${DateTimeUtils.formatDuration(it.durationSeconds)})")
                            }
                        }
                        if (dateMoods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Mood:",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                            dateMoods.forEach {
                                Text("• ${it.mood}${if (it.note.isNotBlank()) " - \"${it.note}\"" else ""}")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedCalendarDate = null }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun InsightItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconColor: androidx.compose.ui.graphics.Color,
    text: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(iconColor.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun MonthlyCalendarCard(
    onSelectDate: (String) -> Unit,
    loggedActivityDates: Set<String>
) {
    val cal = remember { Calendar.getInstance() }
    val currentMonthYear = remember { DateTimeUtils.formatMonthYear(cal) }

    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val year = cal.get(Calendar.YEAR)
    val month = cal.get(Calendar.MONTH) + 1

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("monthly_calendar_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = currentMonthYear,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Day headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Calendar Days Grid
            val daysList = (1..daysInMonth).toList()
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier.height(220.dp),
                userScrollEnabled = false
            ) {
                items(daysList) { dayNum ->
                    val dateStr = String.format("%04d-%02d-%02d", year, month, dayNum)
                    val hasData = loggedActivityDates.contains(dateStr)

                    Column(
                        modifier = Modifier
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelectDate(dateStr) }
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$dayNum",
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (hasData) VitaPrimary else androidx.compose.ui.graphics.Color.Transparent)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(VitaPrimary))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "● Activity logged • Tap any day to inspect details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
