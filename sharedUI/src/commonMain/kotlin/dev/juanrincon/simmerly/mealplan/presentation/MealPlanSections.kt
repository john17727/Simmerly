package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanState
import kotlinx.datetime.LocalDate

/** Everything a plan layout can ask for; the screen turns these into intents, sheets and navigation. */
internal class PlanActions(
    val onEvent: (MealPlanIntent) -> Unit,
    val onOpenRecipe: (String) -> Unit,
    val onPlan: (MealType, LocalDate) -> Unit,
    val onEdit: (MealPlanEntry) -> Unit,
    val onAddToList: (CartRequestScope, Int?) -> Unit,
)

internal fun cookLabelFor(state: MealPlanState): String =
    if (state.selectedDate == state.today) "Cook today" else "Cook ${state.selectedDate.dayOfWeek.displayName()}"

@Composable
internal fun DayCartButton(state: MealPlanState, actions: PlanActions) {
    if (state.canEdit && state.dayRecipeEntries.isNotEmpty()) {
        IconButton(onClick = { actions.onAddToList(CartRequestScope.Day, null) }) {
            Icon(Icons.Rounded.AddShoppingCart, contentDescription = "Add this day's recipes to a shopping list")
        }
    }
}

/** The "This week, any day" entries with their Cook buttons. */
internal fun LazyListScope.anyDayItems(state: MealPlanState, actions: PlanActions, title: String, showCart: Boolean = true) {
    if (state.anyDayEntries.isEmpty()) return
    item(key = "week") {
        val scheme = MaterialTheme.colorScheme
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Rounded.DateRange, null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                Text(
                    title,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    weekRangeLabel(state.weekDays.first(), state.weekDays.last()),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.onSurfaceVariant,
                )
                if (showCart && state.canEdit && state.weekRecipeEntries.isNotEmpty()) {
                    IconButton(onClick = { actions.onAddToList(CartRequestScope.Week, null) }) {
                        Icon(Icons.Rounded.AddShoppingCart, contentDescription = "Add this week's recipes to a shopping list")
                    }
                }
            }
            state.anyDayEntries.forEach { entry ->
                PlanEntryCard(
                    entry = entry,
                    canEdit = state.canEdit,
                    cookLabel = cookLabelFor(state),
                    onCook = { actions.onEvent(MealPlanIntent.CookOnSelectedDay(entry.id)) },
                    onOpenRecipe = { entry.recipe?.let { actions.onOpenRecipe(it.id) } },
                    onServings = { actions.onEvent(MealPlanIntent.ChangeServings(entry.id, it)) },
                    onEdit = { actions.onEdit(entry) },
                    onAddToList = { actions.onAddToList(CartRequestScope.Entry, entry.id) },
                    onRemove = { actions.onEvent(MealPlanIntent.Remove(entry.id)) },
                )
            }
            androidx.compose.material3.HorizontalDivider(color = scheme.outlineVariant, modifier = Modifier.padding(top = 12.dp))
        }
    }
}

/** The selected day's meal sections plus the "Side, snack, drink or dessert" button. */
internal fun LazyListScope.daySectionItems(state: MealPlanState, actions: PlanActions) {
    val day = state.selectedDate
    items(state.daySections.filter { it.type.isMainMeal || it.entries.isNotEmpty() }, key = { it.type }) { section ->
        val scheme = MaterialTheme.colorScheme
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Icon(section.type.icon(), null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    section.type.label(),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                if (state.canEdit && section.entries.isNotEmpty()) {
                    IconButton(onClick = { actions.onPlan(section.type, day) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add to ${section.type.label()}")
                    }
                }
            }
            section.entries.forEach { entry ->
                PlanEntryCard(
                    entry = entry,
                    canEdit = state.canEdit,
                    onOpenRecipe = { entry.recipe?.let { actions.onOpenRecipe(it.id) } },
                    onServings = { actions.onEvent(MealPlanIntent.ChangeServings(entry.id, it)) },
                    onEdit = { actions.onEdit(entry) },
                    onAddToList = { actions.onAddToList(CartRequestScope.Entry, entry.id) },
                    onRemove = { actions.onEvent(MealPlanIntent.Remove(entry.id)) },
                )
            }
            if (section.entries.isEmpty()) {
                if (state.canEdit) PlanSlot("Plan ${section.type.label().lowercase()}", onClick = { actions.onPlan(section.type, day) })
                else Text("Nothing planned", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        }
    }
    if (state.canEdit) {
        item(key = "others") {
            OutlinedButton(onClick = { actions.onPlan(MealType.Side, day) }) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Side, snack, drink or dessert")
            }
        }
    }
}

@Composable
internal fun EmptyWeek(state: MealPlanState, actions: PlanActions, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(88.dp).clip(CircleShape).background(scheme.primaryContainer.copy(alpha = 0.5f)),
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = scheme.primary, modifier = Modifier.size(44.dp))
        }
        Text("Nothing planned this week", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            if (state.canEdit) "Plan a recipe or a note and it will show up here." else "Reconnect to plan meals.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (state.canEdit) {
            Button(onClick = { actions.onPlan(MealType.Dinner, state.selectedDate) }) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Plan a meal")
            }
        }
        if (state.weekDays.none { it == state.today }) {
            TextButton(onClick = { actions.onEvent(MealPlanIntent.GoToToday) }) { Text("Back to this week") }
        }
    }
}

@Composable
internal fun ErrorState(onRetry: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize().padding(32.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(72.dp).clip(CircleShape).background(scheme.errorContainer),
        ) {
            Icon(Icons.Rounded.Error, null, tint = scheme.onErrorContainer, modifier = Modifier.size(36.dp))
        }
        Text("Couldn't load this week", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            "Your Mealie server didn't respond. Check that it's running and reachable, then try again.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.size(8.dp))
            Text("Try again")
        }
    }
}
