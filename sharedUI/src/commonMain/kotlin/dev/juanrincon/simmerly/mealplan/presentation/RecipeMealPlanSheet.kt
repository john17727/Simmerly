package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.juanrincon.simmerly.core.presentation.StepperPill
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddState
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * "Add to meal plan" from a recipe's details: the recipe is fixed, so only when, which meal and
 * servings are asked. A bottom sheet on a phone, the two-column add layout on wider windows.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun RecipeMealPlanSheet(
    recipeId: String,
    onDismiss: () -> Unit,
    onAdded: (MealPlanEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val args = remember(recipeId) { MealPlanAddArgs(date = today, type = MealType.Dinner, recipeId = recipeId) }
    val viewModel: MealPlanAddViewModel = koinViewModel(key = "recipe-meal-plan-${recipeId}") { parametersOf(args) }
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is MealPlanAddSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.asStringSuspend())
            is MealPlanAddSideEffect.Saved -> onAdded(effect.entry)
        }
    }
    val title = "Add to meal plan"
    when {
        isTwoPaneWidth() -> Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(880.dp).heightIn(max = 720.dp),
            ) {
                Box {
                    MealPlanAddWide(state, viewModel::onEvent, state.plannedDays(), onDismiss, title)
                    SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
                }
            }
        }
        isMediumWidthOrWider() -> ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            sheetMaxWidth = 880.dp,
            modifier = modifier,
        ) {
            Box(Modifier.fillMaxHeight(0.94f)) {
                MealPlanAddWide(state, viewModel::onEvent, state.plannedDays(), onDismiss, title)
                SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
            }
        }
        else -> ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = modifier,
        ) {
            Box(Modifier.fillMaxHeight(0.94f)) {
                RecipeMealPlanPhone(state, viewModel::onEvent, onDismiss)
                SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
            }
        }
    }
}

/** Days with something planned, for the calendar dots. */
private fun MealPlanAddState.plannedDays() = weekEntries.filter { !it.isWholeWeek }.map { it.date }.toSet()

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RecipeMealPlanPhone(
    state: MealPlanAddState,
    onEvent: (MealPlanAddIntent) -> Unit,
    onDismiss: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    var month by remember { mutableStateOf(state.date) }
    val recipe = state.allRecipes.firstOrNull { it.id == state.selectedRecipeId }
    Column(Modifier.fillMaxWidth().imePadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 24.dp, end = 8.dp)) {
            Column(Modifier.weight(1f)) {
                Text("Add to meal plan", style = MaterialTheme.typography.headlineSmall)
                if (recipe != null) {
                    Text(recipe.name, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant, maxLines = 1)
                }
            }
            IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            MonthCalendar(
                month = month,
                firstDay = state.firstDay,
                selected = state.date,
                today = state.today,
                plannedDays = state.plannedDays(),
                onSelect = { month = it; onEvent(MealPlanAddIntent.SetDate(it)) },
                onMonthChange = { month = it },
            )
            val days = state.date.weekDays(state.firstDay)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Plan for the whole week", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(weekRangeLabel(days.first(), days.last()), style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }
                Switch(
                    checked = state.wholeWeek,
                    onCheckedChange = { onEvent(MealPlanAddIntent.SetWholeWeek(it)) },
                    thumbContent = if (state.wholeWeek) {
                        { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                )
            }
            HorizontalDivider(color = scheme.outlineVariant)
            Text("Meal", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MealType.entries.forEach { type ->
                    FilterChip(
                        selected = state.type == type,
                        onClick = { onEvent(MealPlanAddIntent.SetType(type)) },
                        label = { Text(type.label()) },
                        leadingIcon = { Icon(type.icon(), null, modifier = Modifier.size(18.dp)) },
                    )
                }
            }
            LockedRecipe(state)
            RecipeExtras(state, onEvent)
        }
        HorizontalDivider(color = scheme.outlineVariant)
        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding()) {
            Text(
                "${state.type.label()}, ${if (state.wholeWeek) "any day this week" else state.date.shortDayLabel()}" +
                    (if (state.date == state.today && !state.wholeWeek) " (today)" else ""),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End), modifier = Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Button(onClick = { onEvent(MealPlanAddIntent.Save) }, enabled = state.canSave) { Text("Add") }
            }
        }
    }
}
