package com.lu4p.fokuslauncher.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.Calendar
import kotlin.math.roundToInt

private const val ACTIVE_DAY_START_MINUTE = 4 * 60 + 45
private const val ACTIVE_DAY_END_MINUTE = 21 * 60 + 45
private const val ACTIVE_DAY_DURATION_MINUTES = ACTIVE_DAY_END_MINUTE - ACTIVE_DAY_START_MINUTE

internal data class DailyProgress(val elapsedFraction: Float) {
    val remainingPercent: Int
        get() = ((1f - elapsedFraction) * 100).roundToInt()
}

/**
 * The user's active-day progress: 04:45–21:45. Keeping this calculation independent from the UI
 * makes the schedule straightforward to adjust and guarantees the bar is clamped at either end.
 */
internal fun dailyProgressAt(calendar: Calendar): DailyProgress {
    val currentMinute =
            calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE)
    val elapsed =
            when {
                currentMinute <= ACTIVE_DAY_START_MINUTE -> 0
                currentMinute >= ACTIVE_DAY_END_MINUTE -> ACTIVE_DAY_DURATION_MINUTES
                else -> currentMinute - ACTIVE_DAY_START_MINUTE
            }
    return DailyProgress(elapsed.toFloat() / ACTIVE_DAY_DURATION_MINUTES)
}

/** A minimal, minute-aligned progress indicator for the configured active day. */
@Composable
fun DailyProgressWidget(modifier: Modifier = Modifier, outlined: Boolean = false) {
    val progress by
            produceState(initialValue = dailyProgressAt(Calendar.getInstance())) {
                while (true) {
                    value = dailyProgressAt(Calendar.getInstance())
                    delay(millisUntilNextMinute())
                }
            }
    val foreground = MaterialTheme.colorScheme.onBackground
    val track = foreground.copy(alpha = 0.26f)
    val labelStyle = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium)

    Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                    imageVector = Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint = foreground,
                    modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(8.dp))
            DailyProgressBar(
                    elapsedFraction = progress.elapsedFraction,
                    filledColor = foreground,
                    trackColor = track,
                    modifier = Modifier.weight(1f),
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                    imageVector = Icons.Outlined.DarkMode,
                    contentDescription = null,
                    tint = foreground,
                    modifier = Modifier.size(16.dp),
            )
        }
        val label = "${progress.remainingPercent}% of your day remaining"
        if (outlined) {
            OutlinedText(text = label, style = labelStyle, color = foreground)
        } else {
            Text(text = label, style = labelStyle, color = foreground)
        }
    }
}

@Composable
private fun DailyProgressBar(
        elapsedFraction: Float,
        filledColor: Color,
        trackColor: Color,
        modifier: Modifier = Modifier,
) {
    val progress = elapsedFraction.coerceIn(0f, 1f)
    BoxWithConstraints(
            contentAlignment = Alignment.CenterStart,
            modifier = modifier.height(12.dp),
    ) {
        Box(
                modifier =
                        Modifier.fillMaxWidth()
                                .height(4.dp)
                                .background(trackColor, CircleShape),
        )
        Box(
                modifier =
                        Modifier.fillMaxWidth(progress)
                                .height(4.dp)
                                .background(filledColor, CircleShape),
        )
        val indicatorDiameter = 8.dp
        val maximumIndicatorOffset = (maxWidth - indicatorDiameter).coerceAtLeast(0.dp)
        val indicatorOffset = ((maxWidth - indicatorDiameter) * progress).coerceIn(0.dp, maximumIndicatorOffset)
        Box(
                modifier =
                        Modifier.offset(x = indicatorOffset)
                                .size(indicatorDiameter)
                                .background(filledColor, CircleShape),
        )
    }
}

private fun millisUntilNextMinute(): Long =
        (60_000L - (System.currentTimeMillis() % 60_000L)).coerceAtLeast(1L)
