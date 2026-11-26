package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.AddTab
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddState
import kotlinx.datetime.LocalDate

/** Two columns: "When" (date, any-day switch, meal) beside "What" (recipe or note). */
@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class)
@Composable
internal fun MealPlanAddWide(
    state: MealPlanAddState,
    onEvent: (MealPlanAddIntent) -> Unit,
    plannedDays: Set<LocalDate>,
    onDismiss: () -> Unit,
    title: String? = null,
) {
    val scheme = MaterialTheme.colorScheme
    var month by remember { mutableStateOf(state.date) }
    Column(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 16.dp)) {
            Text(
                title ?: if (state.isEdit) "Edit meal" else "Plan a meal",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
        }
        Row(Modifier.weight(1f, fill = false).fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.width(352.dp).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text("When", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                MonthCalendar(
                    month = month,
                    firstDay = state.firstDay,
                    selected = state.date,
                    today = state.today,
                    plannedDays = plannedDays,
                    onSelect = { month = it; onEvent(MealPlanAddIntent.SetDate(it)) },
                    onMonthChange = { month = it },
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Any day this week", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                    Switch(
                        checked = state.wholeWeek,
                        onCheckedChange = { onEvent(MealPlanAddIntent.SetWholeWeek(it)) },
                        thumbContent = if (state.wholeWeek) {
                            { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                    )
                }
                Text("Meal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    MealType.entries.forEach { type ->
                        val planned = state.weekEntries.any { e ->
                            e.type == type && e.id != state.editingId &&
                                if (state.wholeWeek) e.isWholeWeek else !e.isWholeWeek && e.date == state.date
                        }
                        PlanFilterChip(
                            selected = state.type == type,
                            onClick = { onEvent(MealPlanAddIntent.SetType(type)) },
                            label = { Text(type.label() + if (planned) " •" else "") },
                            leadingIcon = { Icon(type.icon(), null, modifier = Modifier.size(18.dp)) },
                        )
                    }
                }
            }
            Box(Modifier.fillMaxHeight().width(1.dp).background(scheme.outlineVariant))
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 8.dp),
            ) {
                Text("What", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                if (!state.recipeLocked) {
                    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                        ToggleButton(
                            checked = state.tab == AddTab.Recipe,
                            onCheckedChange = { onEvent(MealPlanAddIntent.SetTab(AddTab.Recipe)) },
                            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.MenuBook, null, modifier = Modifier.size(20.dp))
                            Text("  Recipe")
                        }
                        ToggleButton(
                            checked = state.tab == AddTab.Note,
                            onCheckedChange = { onEvent(MealPlanAddIntent.SetTab(AddTab.Note)) },
                            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Icon(Icons.Rounded.StickyNote2, null, modifier = Modifier.size(20.dp))
                            Text("  Note")
                        }
                    }
                }
                if (state.tab == AddTab.Recipe) {
                    if (!state.recipeLocked) RecipePicker(state, onEvent) else LockedRecipe(state)
                    RecipeExtras(state, onEvent)
                } else {
                    NoteFields(state, onEvent)
                }
            }
        }
        HorizontalDivider(color = scheme.outlineVariant)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            val what = when (state.tab) {
                AddTab.Recipe -> state.allRecipes.firstOrNull { it.id == state.selectedRecipeId }?.name
                AddTab.Note -> state.noteTitle.takeIf { it.isNotBlank() }
            }
            val whenText = if (state.wholeWeek) "Any day this week" else state.date.shortDayLabel()
            Text(
                listOfNotNull(state.type.label(), whenText, what).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Button(onClick = { onEvent(MealPlanAddIntent.Save) }, enabled = state.canSave) {
                Text(
                    when {
                        !state.canSave && !state.isSaving -> if (state.tab == AddTab.Recipe) "Choose a recipe" else "Add a title"
                        state.isEdit -> "Save"
                        state.wholeWeek -> "Add to ${state.type.label().lowercase()}, any day"
                        else -> "Add to ${state.type.label().lowercase()}"
                    }
                )
            }
        }
    }
}
