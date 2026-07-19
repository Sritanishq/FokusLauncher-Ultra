package com.lu4p.fokuslauncher.ui.components

import java.util.Calendar
import org.junit.Assert.assertEquals
import org.junit.Test

class DailyProgressWidgetTest {

    @Test
    fun `clamps progress before and after the active day`() {
        assertEquals(0f, dailyProgressAt(at(hour = 4, minute = 44)).elapsedFraction, 0f)
        assertEquals(0f, dailyProgressAt(at(hour = 4, minute = 45)).elapsedFraction, 0f)
        assertEquals(1f, dailyProgressAt(at(hour = 21, minute = 45)).elapsedFraction, 0f)
        assertEquals(1f, dailyProgressAt(at(hour = 23, minute = 0)).elapsedFraction, 0f)
    }

    @Test
    fun `reports remaining active-day percentage rounded to the nearest whole number`() {
        val progress = dailyProgressAt(at(hour = 11, minute = 8)) // 383 of 1,020 minutes elapsed.

        assertEquals(62, progress.remainingPercent)
    }

    private fun at(hour: Int, minute: Int): Calendar =
            Calendar.getInstance().apply {
                clear()
                set(2026, Calendar.JULY, 19, hour, minute)
            }
}
