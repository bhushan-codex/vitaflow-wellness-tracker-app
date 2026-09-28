package com.example.util

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    private val displayDateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun todayString(): String {
        return dateFormat.format(Date())
    }

    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = dateFormat.parse(dateStr) ?: Date()
            displayDateFormat.format(date)
        } catch (_: Exception) {
            dateStr
        }
    }

    fun formatDisplayDate(timestamp: Long): String {
        return displayDateFormat.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    fun formatMonthYear(calendar: Calendar): String {
        return monthYearFormat.format(calendar.time)
    }

    fun formatDuration(seconds: Long): String {
        val hrs = seconds / 3600
        val mins = (seconds % 3600) / 60
        val secs = seconds % 60
        return if (hrs > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hrs, mins, secs)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", mins, secs)
        }
    }

    fun formatMinutesToHours(minutes: Int): String {
        val hrs = minutes / 60
        val mins = minutes % 60
        return if (hrs > 0 && mins > 0) {
            "${hrs}h ${mins}m"
        } else if (hrs > 0) {
            "${hrs}h"
        } else {
            "${mins}m"
        }
    }

    /**
     * Calculates sleep duration handling overnight crossings past midnight.
     * e.g., Bedtime 23:30 (11:30 PM), Wake 07:15 (7:15 AM)
     * Bed total mins = 23 * 60 + 30 = 1410
     * Wake total mins = 7 * 60 + 15 = 435
     * Difference with midnight wrap: 1440 - 1410 + 435 = 465 mins (7h 45m).
     */
    fun calculateSleepDurationMinutes(bedHour: Int, bedMinute: Int, wakeHour: Int, wakeMinute: Int): Int {
        val bedTotalMins = bedHour * 60 + bedMinute
        val wakeTotalMins = wakeHour * 60 + wakeMinute

        return if (wakeTotalMins >= bedTotalMins) {
            wakeTotalMins - bedTotalMins
        } else {
            (1440 - bedTotalMins) + wakeTotalMins
        }
    }

    fun getDaysAgoString(daysAgo: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return dateFormat.format(cal.time)
    }

    fun getLast7Days(): List<String> {
        return (6 downTo 0).map { getDaysAgoString(it) }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            in 17..21 -> "Good evening"
            else -> "Good night"
        }
    }
}
