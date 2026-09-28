package com.example.data.backup

import com.example.data.database.VitaFlowDatabase
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

data class ImportResult(
    val success: Boolean,
    val totalRecordsImported: Int,
    val message: String
)

object BackupManager {
    suspend fun exportToJson(database: VitaFlowDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()
        root.put("appName", "VitaFlow")
        root.put("version", 1)
        root.put("exportedAt", System.currentTimeMillis())

        val activities = database.activityDao().getAllActivities().first()
        val activityArray = JSONArray()
        for (item in activities) {
            val obj = JSONObject()
            obj.put("type", item.type)
            obj.put("durationMinutes", item.durationMinutes)
            obj.put("steps", item.steps)
            obj.put("distanceMeters", item.distanceMeters)
            obj.put("caloriesEstimate", item.caloriesEstimate)
            obj.put("notes", item.notes)
            obj.put("date", item.date)
            obj.put("timestamp", item.timestamp)
            activityArray.put(obj)
        }
        root.put("activities", activityArray)

        val exercises = database.exerciseDao().getAllSessions().first()
        val exerciseArray = JSONArray()
        for (item in exercises) {
            val obj = JSONObject()
            obj.put("type", item.type)
            obj.put("durationSeconds", item.durationSeconds)
            obj.put("notes", item.notes)
            obj.put("date", item.date)
            obj.put("timestamp", item.timestamp)
            exerciseArray.put(obj)
        }
        root.put("exercises", exerciseArray)

        val water = database.waterDao().getAllWater().first()
        val waterArray = JSONArray()
        for (item in water) {
            val obj = JSONObject()
            obj.put("amountMl", item.amountMl)
            obj.put("date", item.date)
            obj.put("timestamp", item.timestamp)
            waterArray.put(obj)
        }
        root.put("waterEntries", waterArray)

        val sleep = database.sleepDao().getAllSleep().first()
        val sleepArray = JSONArray()
        for (item in sleep) {
            val obj = JSONObject()
            obj.put("bedtimeHour", item.bedtimeHour)
            obj.put("bedtimeMinute", item.bedtimeMinute)
            obj.put("wakeHour", item.wakeHour)
            obj.put("wakeMinute", item.wakeMinute)
            obj.put("durationMinutes", item.durationMinutes)
            obj.put("quality", item.quality)
            obj.put("notes", item.notes)
            obj.put("date", item.date)
            obj.put("timestamp", item.timestamp)
            sleepArray.put(obj)
        }
        root.put("sleepEntries", sleepArray)

        val moods = database.moodDao().getAllMoods().first()
        val moodArray = JSONArray()
        for (item in moods) {
            val obj = JSONObject()
            obj.put("mood", item.mood)
            obj.put("note", item.note)
            obj.put("date", item.date)
            obj.put("timestamp", item.timestamp)
            moodArray.put(obj)
        }
        root.put("moodEntries", moodArray)

        val meals = database.mealDao().getAllMeals().first()
        val mealArray = JSONArray()
        for (item in meals) {
            val obj = JSONObject()
            obj.put("mealType", item.mealType)
            obj.put("foodName", item.foodName)
            obj.put("note", item.note)
            obj.put("timeStr", item.timeStr)
            obj.put("date", item.date)
            obj.put("timestamp", item.timestamp)
            mealArray.put(obj)
        }
        root.put("mealEntries", mealArray)

        val habits = database.habitDao().getAllHabits().first()
        val habitArray = JSONArray()
        for (item in habits) {
            val obj = JSONObject()
            obj.put("name", item.name)
            obj.put("category", item.category)
            obj.put("iconName", item.iconName)
            obj.put("targetPerWeek", item.targetPerWeek)
            obj.put("isActive", item.isActive)
            obj.put("createdAt", item.createdAt)
            habitArray.put(obj)
        }
        root.put("habits", habitArray)

        val completions = database.habitDao().getAllCompletions().first()
        val completionArray = JSONArray()
        for (item in completions) {
            val obj = JSONObject()
            obj.put("habitId", item.habitId)
            obj.put("date", item.date)
            obj.put("completedAt", item.completedAt)
            completionArray.put(obj)
        }
        root.put("habitCompletions", completionArray)

        root.toString(2)
    }

    suspend fun importFromJson(jsonString: String, database: VitaFlowDatabase): ImportResult = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            if (!root.has("appName") || root.getString("appName") != "VitaFlow") {
                return@withContext ImportResult(
                    success = false,
                    totalRecordsImported = 0,
                    message = "Invalid backup file: Not a VitaFlow backup format."
                )
            }

            var recordCount = 0

            if (root.has("activities")) {
                val array = root.getJSONArray("activities")
                val list = mutableListOf<ActivityEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ActivityEntry(
                            type = obj.optString("type", "Walking"),
                            durationMinutes = obj.optInt("durationMinutes", 0),
                            steps = obj.optInt("steps", 0),
                            distanceMeters = obj.optInt("distanceMeters", 0),
                            caloriesEstimate = obj.optInt("caloriesEstimate", 0),
                            notes = obj.optString("notes", ""),
                            date = obj.optString("date", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.activityDao().insertAll(list)
                recordCount += list.size
            }

            if (root.has("exercises")) {
                val array = root.getJSONArray("exercises")
                val list = mutableListOf<ExerciseSession>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        ExerciseSession(
                            type = obj.optString("type", "Walking"),
                            durationSeconds = obj.optLong("durationSeconds", 0),
                            notes = obj.optString("notes", ""),
                            date = obj.optString("date", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.exerciseDao().insertAll(list)
                recordCount += list.size
            }

            if (root.has("waterEntries")) {
                val array = root.getJSONArray("waterEntries")
                val list = mutableListOf<WaterEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        WaterEntry(
                            amountMl = obj.optInt("amountMl", 250),
                            date = obj.optString("date", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.waterDao().insertAll(list)
                recordCount += list.size
            }

            if (root.has("sleepEntries")) {
                val array = root.getJSONArray("sleepEntries")
                val list = mutableListOf<SleepEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        SleepEntry(
                            bedtimeHour = obj.optInt("bedtimeHour", 23),
                            bedtimeMinute = obj.optInt("bedtimeMinute", 0),
                            wakeHour = obj.optInt("wakeHour", 7),
                            wakeMinute = obj.optInt("wakeMinute", 0),
                            durationMinutes = obj.optInt("durationMinutes", 480),
                            quality = obj.optString("quality", "Good"),
                            notes = obj.optString("notes", ""),
                            date = obj.optString("date", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.sleepDao().insertAll(list)
                recordCount += list.size
            }

            if (root.has("moodEntries")) {
                val array = root.getJSONArray("moodEntries")
                val list = mutableListOf<MoodEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MoodEntry(
                            mood = obj.optString("mood", "Good"),
                            note = obj.optString("note", ""),
                            date = obj.optString("date", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.moodDao().insertAll(list)
                recordCount += list.size
            }

            if (root.has("mealEntries")) {
                val array = root.getJSONArray("mealEntries")
                val list = mutableListOf<MealEntry>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    list.add(
                        MealEntry(
                            mealType = obj.optString("mealType", "Lunch"),
                            foodName = obj.optString("foodName", ""),
                            note = obj.optString("note", ""),
                            timeStr = obj.optString("timeStr", ""),
                            date = obj.optString("date", ""),
                            timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                        )
                    )
                }
                database.mealDao().insertAll(list)
                recordCount += list.size
            }

            ImportResult(
                success = true,
                totalRecordsImported = recordCount,
                message = "Successfully imported $recordCount records."
            )
        } catch (e: Exception) {
            ImportResult(
                success = false,
                totalRecordsImported = 0,
                message = "Failed to parse import data: ${e.localizedMessage ?: "Unknown error"}"
            )
        }
    }
}
