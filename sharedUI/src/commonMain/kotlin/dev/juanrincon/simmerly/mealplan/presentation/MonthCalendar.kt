package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

/**
 * A month grid with a dot under days that have something planned, the week of [selected] drawn as
 * a band, and a ring on [today]. Days of the neighbouring months are greyed; tapping one selects it
 * (and the caller moves the month). M3's DatePicker can't draw per-day content, hence this.
 */
@Composable
fun MonthCalendar(
    month: LocalDate,
    firstDay: DayOfWeek,
    selected: LocalDate,
    today: LocalDate,
    plannedDays: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit,
    onMonthChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val first = LocalDate(month.year, month.month, 1)
    val gridStart = first.monthGridStart(firstDay)
    val weekDays = selected.weekDays(firstDay)
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                first.monthYearLabel(),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f).padding(start = 8.dp),
            )
            IconButton(onClick = { onMonthChange(first.shiftMonths(-1)) }) {
                Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous month")
            }
            IconButton(onClick = { onMonthChange(first.shiftMonths(1)) }) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = "Next month")
            }
        }
        Row(Modifier.fillMaxWidth()) {
            weekDays.forEach { d ->
                Text(
                    d.dayOfWeek.shortName().take(1),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f).padding(vertical = 4.dp),
                )
            }
        }
        for (row in 0 until 6) {
            Row(Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val day = gridStart.plusDays(row * 7 + col)
                    val inMonth = day.month == first.month
                    val inBand = day in weekDays
                    val isSelected = day == selected
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .background(if (inBand) scheme.primaryContainer.copy(alpha = 0.45f) else androidx.compose.ui.graphics.Color.Transparent)
                            .clickable(role = Role.Button) { onSelect(day) },
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) scheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                                    .then(
                                        if (day == today && !isSelected)
                                            Modifier.border(1.5.dp, scheme.primary, CircleShape)
                                        else Modifier
                                    ),
                            ) {
                                Text(
                                    day.day.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected || day == today) FontWeight.SemiBold else FontWeight.Normal,
                                    color = when {
                                        isSelected -> scheme.onPrimary
                                        inMonth -> scheme.onSurface
                                        else -> scheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    },
                                )
                            }
                            Box(
                                Modifier
                                    .padding(top = 1.dp)
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (day in plannedDays) scheme.primary
                                        else androidx.compose.ui.graphics.Color.Transparent
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

internal fun LocalDate.shiftMonths(months: Int): LocalDate {
    val index = year * 12 + (month.ordinal) + months
    return LocalDate(index / 12, Month(index % 12 + 1), 1)
}
