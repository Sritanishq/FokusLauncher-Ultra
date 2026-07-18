package com.lu4p.fokuslauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.lu4p.fokuslauncher.ui.util.clickableNoRippleWithSystemSound
import java.text.DateFormatSymbols
import java.util.Calendar
import java.util.Locale

/**
 * A compact, locale-aware view of the current month. It intentionally leaves dates from adjacent
 * months blank so the home screen stays quiet and easy to scan.
 */
@Composable
fun MonthlyCalendar(
        modifier: Modifier = Modifier,
        outlined: Boolean = false,
        onClick: () -> Unit = {},
) {
    val locale = Locale.getDefault()
    val today = Calendar.getInstance()
    val month = calendarMonth(today, locale)
    val textColor = MaterialTheme.colorScheme.onBackground
    val weekdayColor = textColor.copy(alpha = 0.58f)
    val dayStyle = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
    val weekdayStyle = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold)
    val monthStyle = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)

    Column(
            modifier =
                    modifier
                            .fillMaxWidth()
                            .clickableNoRippleWithSystemSound(onClick = onClick)
                            .testTag("monthly_calendar"),
            verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (outlined) {
            OutlinedText(text = month.title, style = monthStyle, color = textColor)
        } else {
            Text(text = month.title, style = monthStyle, color = textColor)
        }

        CalendarWeek(
                labels = month.weekdayLabels,
                textColor = weekdayColor,
                textStyle = weekdayStyle,
                outlined = outlined,
        )
        month.days.chunked(DAYS_IN_WEEK).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { day ->
                    CalendarDay(
                            day = day,
                            textColor = textColor,
                            textStyle = dayStyle,
                            outlined = outlined,
                            modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarWeek(
        labels: List<String>,
        textColor: androidx.compose.ui.graphics.Color,
        textStyle: androidx.compose.ui.text.TextStyle,
        outlined: Boolean,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        labels.forEach { label ->
            if (outlined) {
                Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.weight(1f),
                ) {
                    OutlinedText(text = label, style = textStyle, color = textColor)
                }
            } else {
                Text(
                        text = label,
                        style = textStyle,
                        color = textColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun CalendarDay(
        day: CalendarDay,
        textColor: androidx.compose.ui.graphics.Color,
        textStyle: androidx.compose.ui.text.TextStyle,
        outlined: Boolean,
        modifier: Modifier = Modifier,
) {
    Box(
            contentAlignment = Alignment.Center,
            modifier =
                    modifier.height(28.dp).then(
                            if (day.isToday) {
                                Modifier.clip(MaterialTheme.shapes.small)
                                        .background(textColor.copy(alpha = 0.16f))
                            } else {
                                Modifier
                            },
                    ),
    ) {
        day.number?.let { number ->
            if (outlined) {
                OutlinedText(text = number.toString(), style = textStyle, color = textColor)
            } else {
                Text(text = number.toString(), style = textStyle, color = textColor)
            }
        }
    }
}

internal data class CalendarMonth(
        val title: String,
        val weekdayLabels: List<String>,
        val days: List<CalendarDay>,
)

internal data class CalendarDay(val number: Int?, val isToday: Boolean = false)

/** Builds a Monday/Sunday/etc. ordered grid according to the user's locale setting. */
internal fun calendarMonth(today: Calendar, locale: Locale): CalendarMonth {
    val firstOfMonth = (today.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val symbols = DateFormatSymbols.getInstance(locale)
    val firstDayOfWeek = firstOfMonth.firstDayOfWeek
    val weekdayLabels =
            (0 until DAYS_IN_WEEK).map { offset ->
                val dayOfWeek = (firstDayOfWeek - Calendar.SUNDAY + offset) % DAYS_IN_WEEK + Calendar.SUNDAY
                symbols.shortWeekdays[dayOfWeek]
            }
    val leadingEmptyDays =
            (firstOfMonth.get(Calendar.DAY_OF_WEEK) - firstDayOfWeek + DAYS_IN_WEEK) % DAYS_IN_WEEK
    val daysInMonth = firstOfMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val days = buildList {
        repeat(leadingEmptyDays) { add(CalendarDay(number = null)) }
        for (day in 1..daysInMonth) {
            add(CalendarDay(number = day, isToday = day == today.get(Calendar.DAY_OF_MONTH)))
        }
        while (size % DAYS_IN_WEEK != 0) add(CalendarDay(number = null))
    }

    return CalendarMonth(
            title = "${symbols.months[firstOfMonth.get(Calendar.MONTH)]} ${firstOfMonth.get(Calendar.YEAR)}",
            weekdayLabels = weekdayLabels,
            days = days,
    )
}

private const val DAYS_IN_WEEK = 7
