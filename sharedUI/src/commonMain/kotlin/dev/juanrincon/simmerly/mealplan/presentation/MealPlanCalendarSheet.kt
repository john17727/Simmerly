package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

/** "Jump to a date": a month with dots for days that have meals and the viewed week as a band. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanCalendarSheet(
    selected: LocalDate,
    today: LocalDate,
    firstDay: DayOfWeek,
    plannedDays: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit,
    onToday: () -> Unit,
    onDismiss: () -> Unit,
) {
    var month by remember { mutableStateOf(selected) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp).navigationBarsPadding(),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("Jump to a date", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                TextButton(onClick = onToday) { Text("Today") }
            }
            MonthCalendar(
                month = month,
                firstDay = firstDay,
                selected = selected,
                today = today,
                plannedDays = plannedDays,
                onSelect = onSelect,
                onMonthChange = { month = it },
            )
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    "Dots mark days with meals planned. The band is the week you're viewing.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
