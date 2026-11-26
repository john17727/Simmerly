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
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.CartScope
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartArgs
import kotlinx.coroutines.launch
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
    onOpenShoppingList: (listId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealPlanViewModel = koinViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var addArgs by remember { mutableStateOf<MealPlanAddArgs?>(null) }
    var showCalendar by remember { mutableStateOf(false) }
    var cartRequest by remember { mutableStateOf<MealPlanCartRequest?>(null) }
    val scope = androidx.compose.runtime.rememberCoroutineScope()

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

    val actions = PlanActions(
        onEvent = viewModel::onEvent,
        onOpenRecipe = onOpenRecipe,
        onPlan = { type, date -> addArgs = MealPlanAddArgs(date = date, type = type) },
        onEdit = { entry ->
            addArgs = MealPlanAddArgs(date = state.selectedDate, type = entry.type, entryId = entry.id)
        },
        onAddToList = { scope, entryId -> cartRequest = MealPlanCartRequest(state.selectedDate, scope, entryId) },
    )
    when {
        isTwoPaneWidth() -> MealPlanDesktop(state, actions, snackbarHostState, modifier)
        isMediumWidthOrWider() -> MealPlanFoldable(state, actions, snackbarHostState, modifier)
        else -> MealPlanPhone(state, actions, snackbarHostState, onOpenCalendar = { showCalendar = true }, modifier = modifier)
    }

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
    cartRequest?.let { request ->
        MealPlanCartSheet(
            args = MealPlanCartArgs(
                date = request.date,
                scope = if (request.scope == CartRequestScope.Week) CartScope.Week else CartScope.Day,
                entryId = request.entryId,
            ),
            onDismiss = { cartRequest = null },
            onAdded = { message, listId ->
                cartRequest = null
                scope.launch {
                    val result = snackbarHostState.showSnackbar(
                        message = message.asStringSuspend(),
                        actionLabel = "View list",
                        duration = SnackbarDuration.Long,
                    )
                    if (result == SnackbarResult.ActionPerformed) onOpenShoppingList(listId)
                }
            },
            onError = { snackbarHostState.showSnackbar(it.asStringSuspend()) },
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
internal fun MealPlanPhone(
    state: MealPlanState,
    actions: PlanActions,
    snackbarHostState: SnackbarHostState,
    onOpenCalendar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val onEvent = actions.onEvent
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
                    PlanList(state, actions)
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
private fun PlanList(state: MealPlanState, actions: PlanActions) {
    LazyColumn(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        if (state.isOffline) {
            item(key = "offline") { OfflineBanner(onRetry = { actions.onEvent(MealPlanIntent.Refresh) }) }
        }
        if (!state.isLoading && state.isEmptyWeek) {
            item(key = "empty") { EmptyWeek(state, actions) }
            return@LazyColumn
        }
        anyDayItems(state, actions, title = "This week")
        item(key = "dayTitle") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(state.selectedDate.dayTitle(), style = MaterialTheme.typography.headlineSmall)
                if (state.selectedDate == state.today) TodayBadge()
                Spacer(Modifier.weight(1f))
                DayCartButton(state, actions)
            }
        }
        daySectionItems(state, actions)
    }
}

