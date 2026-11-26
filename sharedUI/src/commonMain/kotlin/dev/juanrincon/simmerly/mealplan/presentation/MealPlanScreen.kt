package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanState
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/** The Meal plan tab on a phone: week strip, any-day entries, and the selected day's meals. */
@Composable
fun MealPlanScreen(
    onOpenRecipe: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealPlanViewModel = koinViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var addArgs by remember { mutableStateOf<MealPlanAddArgs?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    var cartScope by remember { mutableStateOf<MealPlanCartRequest?>(null) }

    viewModel.collectSideEffect { effect ->
        when (effect) {
            is MealPlanSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.asStringSuspend())
            is MealPlanSideEffect.ShowUndo -> {
                val result = snackbarHostState.showSnackbar(
                    message = effect.message.asStringSuspend(),
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.onEvent(effect.undo)
            }
        }
    }

    MealPlanContent(
        state = state,
        onEvent = viewModel::onEvent,
        snackbarHostState = snackbarHostState,
        onOpenRecipe = onOpenRecipe,
        onPlan = { type -> addArgs = MealPlanAddArgs(date = state.selectedDate, type = type) },
        onEdit = { entry ->
            addArgs = MealPlanAddArgs(date = state.selectedDate, type = entry.type, entryId = entry.id)
        },
        onOpenCalendar = { showCalendar = true },
        onAddToList = { scope, entryId -> cartScope = MealPlanCartRequest(state.selectedDate, scope, entryId) },
        modifier = modifier,
    )

    if (showCalendar) {
        MealPlanCalendarSheet(
            selected = state.selectedDate,
            today = state.today,
            firstDay = state.firstDay,
            plannedDays = state.plannedDays,
            onSelect = { viewModel.onEvent(MealPlanIntent.SelectDate(it)); showCalendar = false },
            onToday = { viewModel.onEvent(MealPlanIntent.GoToToday); showCalendar = false },
            onDismiss = { showCalendar = false },
        )
    }
    addArgs?.let { args ->
        MealPlanAddSheet(
            args = args,
            plannedDays = state.plannedDays,
            onDismiss = { addArgs = null },
            onSaved = { entry ->
                addArgs = null
                // Follow the entry so the user sees what they just planned.
                if (!entry.isWholeWeek && entry.date != state.selectedDate) {
                    viewModel.onEvent(MealPlanIntent.SelectDate(entry.date))
                }
            },
        )
    }
}

/** What the shopping-list cart should open on; wired in a later step. */
data class MealPlanCartRequest(val date: LocalDate, val scope: CartRequestScope, val entryId: Int?)

enum class CartRequestScope { Day, Week, Entry }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MealPlanContent(
    state: MealPlanState,
    onEvent: (MealPlanIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    onOpenRecipe: (String) -> Unit,
    onPlan: (MealType) -> Unit,
    onEdit: (MealPlanEntry) -> Unit,
    onOpenCalendar: () -> Unit,
    onAddToList: (CartRequestScope, Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.fillMaxSize().background(scheme.surface)) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 4.dp),
            ) {
                Text("Meal plan", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                IconButton(onClick = onOpenCalendar) {
                    Icon(Icons.Rounded.CalendarMonth, contentDescription = "Jump to a date")
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp),
            ) {
                Text(
                    weekRangeLabel(state.weekDays.first(), state.weekDays.last()),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = scheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onEvent(MealPlanIntent.GoToToday) }) { Text("Today") }
                IconButton(onClick = { onEvent(MealPlanIntent.StepWeeks(-1)) }) {
                    Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous week")
                }
                IconButton(onClick = { onEvent(MealPlanIntent.StepWeeks(1)) }) {
                    Icon(Icons.Rounded.ChevronRight, contentDescription = "Next week")
                }
            }
            WeekStrip(
                days = state.weekDays,
                selected = state.selectedDate,
                today = state.today,
                plannedDays = state.plannedDays,
                onSelect = { onEvent(MealPlanIntent.SelectDate(it)) },
            )
            androidx.compose.material3.HorizontalDivider(color = scheme.outlineVariant)
            if (state.isLoading || state.isRefreshing) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            when {
                state.hasError -> ErrorState(onRetry = { onEvent(MealPlanIntent.Refresh) })

                else -> PullToRefreshBox(
                    isRefreshing = state.isPullRefreshing,
                    onRefresh = { onEvent(MealPlanIntent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    PlanList(state, onEvent, onOpenRecipe, onPlan, onEdit, onAddToList)
                }
            }
        }
        SnackbarHost(
            snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp),
        )
    }
}

@Composable
private fun PlanList(
    state: MealPlanState,
    onEvent: (MealPlanIntent) -> Unit,
    onOpenRecipe: (String) -> Unit,
    onPlan: (MealType) -> Unit,
    onEdit: (MealPlanEntry) -> Unit,
    onAddToList: (CartRequestScope, Int?) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val day = state.selectedDate
    val cookLabel = if (day == state.today) "Cook today" else "Cook ${day.dayOfWeek.displayName()}"
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.isOffline) {
            item(key = "offline") { OfflineBanner(onRetry = { onEvent(MealPlanIntent.Refresh) }) }
        }
        if (!state.isLoading && state.isEmptyWeek) {
            item(key = "empty") {
                EmptyWeek(
                    showBackToWeek = state.weekDays.none { it == state.today },
                    canEdit = state.canEdit,
                    onPlan = { onPlan(MealType.Dinner) },
                    onBack = { onEvent(MealPlanIntent.GoToToday) },
                )
            }
            return@LazyColumn
        }
        if (state.anyDayEntries.isNotEmpty()) {
            item(key = "week") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Rounded.DateRange, null, tint = scheme.primary, modifier = Modifier.size(18.dp))
                        Text(
                            "This week",
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
                    }
                    state.anyDayEntries.forEach { entry ->
                        PlanEntryCard(
                            entry = entry,
                            canEdit = state.canEdit,
                            cookLabel = cookLabel,
                            onCook = { onEvent(MealPlanIntent.CookOnSelectedDay(entry.id)) },
                            onOpenRecipe = { entry.recipe?.let { onOpenRecipe(it.id) } },
                            onServings = { onEvent(MealPlanIntent.ChangeServings(entry.id, it)) },
                            onEdit = { onEdit(entry) },
                            onAddToList = { onAddToList(CartRequestScope.Entry, entry.id) },
                            onRemove = { onEvent(MealPlanIntent.Remove(entry.id)) },
                        )
                    }
                    androidx.compose.material3.HorizontalDivider(
                        color = scheme.outlineVariant,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
        }
        item(key = "dayTitle") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(day.dayTitle(), style = MaterialTheme.typography.headlineSmall)
                if (day == state.today) TodayBadge()
                Spacer(Modifier.weight(1f))
                if (state.canEdit && state.dayRecipeEntries.isNotEmpty()) {
                    IconButton(onClick = { onAddToList(CartRequestScope.Day, null) }) {
                        Icon(Icons.Rounded.AddShoppingCart, contentDescription = "Add this day's recipes to a shopping list")
                    }
                }
            }
        }
        items(state.daySections.filter { it.type.isMainMeal || it.entries.isNotEmpty() }, key = { it.type }) { section ->
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
                        IconButton(onClick = { onPlan(section.type) }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add to ${section.type.label()}")
                        }
                    }
                }
                section.entries.forEach { entry ->
                    PlanEntryCard(
                        entry = entry,
                        canEdit = state.canEdit,
                        onOpenRecipe = { entry.recipe?.let { onOpenRecipe(it.id) } },
                        onServings = { onEvent(MealPlanIntent.ChangeServings(entry.id, it)) },
                        onEdit = { onEdit(entry) },
                        onAddToList = { onAddToList(CartRequestScope.Entry, entry.id) },
                        onRemove = { onEvent(MealPlanIntent.Remove(entry.id)) },
                    )
                }
                if (section.entries.isEmpty()) {
                    if (state.canEdit) {
                        PlanSlot("Plan ${section.type.label().lowercase()}", onClick = { onPlan(section.type) })
                    } else {
                        Text("Nothing planned", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                }
            }
        }
        if (state.canEdit) {
            item(key = "others") {
                OutlinedButton(onClick = { onPlan(MealType.Side) }) {
                    Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Side, snack, drink or dessert")
                }
            }
        }
    }
}

@Composable
private fun EmptyWeek(
    showBackToWeek: Boolean,
    canEdit: Boolean,
    onPlan: () -> Unit,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 48.dp, horizontal = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(88.dp).clip(CircleShape).background(scheme.primaryContainer.copy(alpha = 0.5f)),
        ) {
            Icon(Icons.Outlined.CalendarMonth, null, tint = scheme.primary, modifier = Modifier.size(44.dp))
        }
        Text("Nothing planned this week", style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
        Text(
            if (canEdit) "Plan a recipe or a note and it will show up here." else "Reconnect to plan meals.",
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (canEdit) {
            Button(onClick = onPlan) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Plan a meal")
            }
        }
        if (showBackToWeek) TextButton(onClick = onBack) { Text("Back to this week") }
    }
}

@Composable
private fun ErrorState(onRetry: () -> Unit) {
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
