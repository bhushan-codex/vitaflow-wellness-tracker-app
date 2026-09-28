package com.example

import com.example.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class DateTimeUtilsTest {

    @Test
    fun testSleepDurationNormalHours() {
        // Bedtime 22:00, Wake 06:00 -> 8 hours = 480 mins
        val duration = DateTimeUtils.calculateSleepDurationMinutes(22, 0, 6, 0)
        assertEquals(480, duration)
    }

    @Test
    fun testSleepDurationMidnightCrossing() {
        // Bedtime 23:30, Wake 07:15 -> 7 hours 45 mins = 465 mins
        val duration = DateTimeUtils.calculateSleepDurationMinutes(23, 30, 7, 15)
        assertEquals(465, duration)
    }

    @Test
    fun testSleepDurationAfterMidnight() {
        // Bedtime 01:15, Wake 08:45 -> 7 hours 30 mins = 450 mins
        val duration = DateTimeUtils.calculateSleepDurationMinutes(1, 15, 8, 45)
        assertEquals(450, duration)
    }

    @Test
    fun testFormatMinutesToHours() {
        assertEquals("8h", DateTimeUtils.formatMinutesToHours(480))
        assertEquals("7h 30m", DateTimeUtils.formatMinutesToHours(450))
        assertEquals("45m", DateTimeUtils.formatMinutesToHours(45))
    }

    @Test
    fun testFormatDurationSeconds() {
        assertEquals("00:30", DateTimeUtils.formatDuration(30))
        assertEquals("05:15", DateTimeUtils.formatDuration(315))
        assertEquals("01:15:30", DateTimeUtils.formatDuration(4530))
    }
}
