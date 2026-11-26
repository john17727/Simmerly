package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.ShoppingBasket
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanState

/**
 * Unfolded foldable: a calendar and the week's any-day entries on the left, the selected day on the
 * right. The calendar is always visible, so there is no jump-to-date sheet.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MealPlanFoldable(
    state: MealPlanState,
    actions: PlanActions,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    val day = state.selectedDate
    // The shown month follows the selection when it moves to another month; chevrons move it freely.
    var month by remember(day.year, day.month) { mutableStateOf(day) }
    Box(modifier.fillMaxSize().background(scheme.surface)) {
        Row(Modifier.fillMaxSize().statusBarsPadding().padding(start = 8.dp, end = 24.dp, top = 16.dp)) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 96.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.width(424.dp).fillMaxHeight(),
            ) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Meal plan", style = MaterialTheme.typography.headlineLarge, modifier = Modifier.weight(1f))
                        TextButton(onClick = { actions.onEvent(dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent.GoToToday) }) {
                            Text("Today")
                        }
                    }
                }
                item {
                    MonthCalendar(
                        month = month,
                        firstDay = state.firstDay,
                        selected = day,
                        today = state.today,
                        plannedDays = state.plannedDays,
                        onSelect = {
                            month = it
                            actions.onEvent(dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent.SelectDate(it))
                        },
                        onMonthChange = { month = it },
                    )
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        LegendDot(scheme.primary, "Has meals")
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.size(width = 16.dp, height = 8.dp).clip(CircleShape).background(scheme.primaryContainer))
                            Text("Week you're viewing", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                        }
                    }
                }
                if (state.canEdit && state.weekRecipeEntries.isNotEmpty()) {
                    item(key = "weekCart") {
                        val shape = RoundedCornerShape(12.dp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth().clip(shape).background(scheme.surfaceContainerLow)
                                .border(1.dp, scheme.outlineVariant, shape).padding(start = 16.dp, top = 10.dp, bottom = 10.dp, end = 10.dp),
                        ) {
                            Icon(Icons.Rounded.ShoppingBasket, null, tint = scheme.onSurfaceVariant)
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "Week of ${weekRangeLabel(state.weekDays.first(), state.weekDays.last())}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                val n = state.weekRecipeEntries.size
                                Text(
                                    "$n ${if (n == 1) "recipe" else "recipes"} planned",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = scheme.onSurfaceVariant,
                                )
                            }
                            FilledTonalButton(onClick = { actions.onAddToList(CartRequestScope.Week, null) }) {
                                Icon(Icons.Rounded.AddShoppingCart, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(8.dp))
                                Text("Add week to list")
                            }
                        }
                    }
                }
                anyDayItems(state, actions, title = "This week, any day", showCart = false)
            }
            Spacer(Modifier.width(48.dp))
            Column(Modifier.weight(1f).fillMaxHeight()) {
                DayHeader(state, actions)
                if (state.isLoading || state.isRefreshing) LinearProgressIndicator(Modifier.fillMaxWidth())
                when {
                    state.hasError -> ErrorState(onRetry = { actions.onEvent(dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent.Refresh) })
                    else -> PullToRefreshBox(
                        isRefreshing = state.isPullRefreshing,
                        onRefresh = { actions.onEvent(dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent.Refresh) },
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        LazyColumn(
                            contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp),
                            verticalArrangement = Arrangement.spacedBy(20.dp),
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (state.isOffline) item(key = "offline") {
                                OfflineBanner(onRetry = { actions.onEvent(dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanIntent.Refresh) })
                            }
                            if (!state.isLoading && state.isEmptyWeek) {
                                item(key = "empty") { EmptyWeek(state, actions) }
                            } else {
                                daySectionItems(state, actions)
                            }
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp))
    }
}

@Composable
private fun LegendDot(color: androidx.compose.ui.graphics.Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.size(6.dp).clip(CircleShape).background(color))
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Day title with Today badge, the day's cart and "Plan a meal". */
@Composable
private fun DayHeader(state: MealPlanState, actions: PlanActions) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
    ) {
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(state.selectedDate.dayTitle(), style = MaterialTheme.typography.headlineMedium)
            if (state.selectedDate == state.today) TodayBadge()
        }
        DayCartButton(state, actions)
        if (state.canEdit) {
            Button(onClick = { actions.onPlan(dev.juanrincon.simmerly.mealplan.domain.MealType.Dinner, state.selectedDate) }) {
                Icon(Icons.Rounded.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text("Plan a meal")
            }
        }
    }
}
