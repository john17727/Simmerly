package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Checklist
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.mealplan.presentation.orbit.CartEntry
import dev.juanrincon.simmerly.mealplan.presentation.orbit.CartRow
import dev.juanrincon.simmerly.mealplan.presentation.orbit.CartScope
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListRowUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/** "Add meal plan to shopping list": pick recipes, a list and ingredients, then add. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanCartSheet(
    args: MealPlanCartArgs,
    onDismiss: () -> Unit,
    onAdded: (message: UiText, listId: String) -> Unit,
    onError: suspend (UiText) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealPlanCartViewModel = koinViewModel(key = "meal-plan-cart-${args}") { parametersOf(args) },
) {
    val state by viewModel.collectAsState()
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is MealPlanCartSideEffect.ShowMessage -> onError(effect.message)
            is MealPlanCartSideEffect.Added -> onAdded(effect.message, effect.listId)
        }
    }
    if (isMediumWidthOrWider()) {
        Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(720.dp).heightIn(max = 760.dp).padding(vertical = 16.dp),
            ) {
                MealPlanCartContent(state, viewModel::onEvent, showScopes = args.entryId == null, onDismiss = onDismiss, tall = false)
            }
        }
    } else {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = modifier,
        ) {
            MealPlanCartContent(state, viewModel::onEvent, showScopes = args.entryId == null, onDismiss = onDismiss)
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun MealPlanCartContent(
    state: MealPlanCartState,
    onEvent: (MealPlanCartIntent) -> Unit,
    showScopes: Boolean,
    onDismiss: () -> Unit,
    tall: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().then(if (tall) Modifier.fillMaxHeight(0.94f) else Modifier)) {
        Text(
            "Add meal plan to shopping list",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = if (tall) 0.dp else 24.dp),
        )
        LazyColumn(
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = tall),
        ) {
            if (showScopes) {
                item(key = "scopes") {
                    Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                        ToggleButton(
                            checked = state.scope == CartScope.Day,
                            onCheckedChange = { onEvent(MealPlanCartIntent.SetScope(CartScope.Day)) },
                            shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("This day")
                                Text(state.date.shortDayLabel(), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        ToggleButton(
                            checked = state.scope == CartScope.Week,
                            onCheckedChange = { onEvent(MealPlanCartIntent.SetScope(CartScope.Week)) },
                            shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                            modifier = Modifier.weight(1f),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("This week")
                                Text(weekRangeLabel(state.weekStart, state.weekEnd), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Recipes
            item(key = "recipesHeader") {
                SectionHeader(
                    "Recipes",
                    "${state.includedEntries.size} of ${state.candidates.size} included",
                )
            }
            if (state.isLoading) {
                item(key = "loading") { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            }
            if (!state.isLoading && state.candidates.isEmpty()) {
                item(key = "noRecipes") {
                    Text(
                        "No recipes planned here yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
            items(state.candidates, key = { "entry-${it.entry.id}" }) { c -> EntryRow(c) { onEvent(MealPlanCartIntent.ToggleEntry(c.entry.id)) } }
            items(state.notes, key = { "note-${it.id}" }) { note ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                ) {
                    Icon(Icons.Rounded.StickyNote2, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
                    Column {
                        Text(note.title, style = MaterialTheme.typography.bodyMedium)
                        Text("Note, nothing to buy", style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                    }
                }
            }

            // Shopping list
            item(key = "listHeader") { SectionHeader("Shopping list", null) }
            item(key = "filter") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ListFilter.entries.forEach { filter ->
                        PlanFilterChip(
                            selected = state.filter == filter,
                            onClick = { onEvent(MealPlanCartIntent.SetFilter(filter)) },
                            label = { Text(if (filter == ListFilter.Mine) "Mine" else "Household") },
                            leadingIcon = if (state.filter == filter) {
                                { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp)) }
                            } else null,
                        )
                    }
                }
            }
            item(key = "newList") {
                ListOption(
                    icon = Icons.Rounded.Add,
                    title = "New list",
                    subtitle = null,
                    selected = state.creatingNew,
                    onClick = { onEvent(MealPlanCartIntent.SelectNewList) },
                )
            }
            if (state.creatingNew) {
                item(key = "newName") {
                    OutlinedTextField(
                        value = state.newListName,
                        onValueChange = { onEvent(MealPlanCartIntent.SetNewListName(it)) },
                        label = { Text("List name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            val lists = state.lists
            if (lists == null) {
                item(key = "listsLoading") { Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { CircularProgressIndicator() } }
            } else {
                items(lists, key = { "list-${it.id}" }) { row ->
                    ListOption(
                        icon = Icons.Rounded.ShoppingCart,
                        title = row.name,
                        subtitle = if (row.alreadyHasRecipe) "Already has a recipe from this plan" else if (row.itemCount == 0) "Empty" else "${row.itemCount} items",
                        selected = (state.target as? AddToListTarget.Existing)?.id == row.id,
                        onClick = { onEvent(MealPlanCartIntent.SelectList(row.id)) },
                    )
                }
            }

            // Ingredients
            item(key = "ingredientsHeader") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Ingredients", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            "${state.selectedCount} of ${state.rows.size} selected",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    TextButton(onClick = { onEvent(MealPlanCartIntent.ToggleAll) }, enabled = state.rows.isNotEmpty()) {
                        Text(if (state.allSelected) "Deselect all" else "Select all")
                    }
                }
            }
            item(key = "combine") {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("Combine matching ingredients", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                        Text(
                            "Adds up amounts shared by more than one recipe",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = state.combine,
                        onCheckedChange = { onEvent(MealPlanCartIntent.SetCombine(it)) },
                        thumbContent = if (state.combine) {
                            { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                    )
                }
            }
            if (state.rows.isEmpty()) {
                item(key = "noIngredients") {
                    Text(
                        "Pick at least one recipe to see its ingredients.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp),
                    )
                }
            }
            state.groups.forEachIndexed { index, group ->
                val groupEntry = group.entry
                if (groupEntry != null) {
                    item(key = "groupHeader-${index}") {
                        Text(
                            "${groupEntry.recipe?.name.orEmpty()} · ${servingsLabel(groupEntry.servings ?: 1.0)}",
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                    }
                }
                items(group.rows, key = { "row-${it.key}" }) { row ->
                    IngredientRow(row) { onEvent(MealPlanCartIntent.ToggleRow(row.key)) }
                }
            }
        }
        HorizontalDivider(color = scheme.outlineVariant)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(),
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Button(onClick = { onEvent(MealPlanCartIntent.Confirm) }, enabled = state.canAdd) {
                Icon(Icons.Rounded.AddShoppingCart, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(
                    when {
                        state.isSubmitting -> "Adding…"
                        state.selectedCount == 0 -> "Select ingredients to add"
                        state.selectedCount == 1 -> "Add 1 item"
                        else -> "Add ${state.selectedCount} items"
                    }
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, trailing: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
        if (trailing != null) {
            Text(trailing, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EntryRow(c: CartEntry, onToggle: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val recipe = c.entry.recipe ?: return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable(onClick = onToggle).padding(vertical = 4.dp, horizontal = 4.dp),
    ) {
        Checkbox(checked = c.included, onCheckedChange = { onToggle() })
        SubcomposeAsyncImage(
            model = recipe.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(scheme.surfaceContainer),
        )
        Column(Modifier.weight(1f)) {
            Text(
                recipe.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (c.included) scheme.onSurface else scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val whenText = if (c.unscheduled) "any day, not scheduled yet" else c.entry.date.shortDayLabel()
            Text(
                "${c.entry.type.label()} · ${whenText} · ${servingsLabel(c.entry.servings ?: 1.0)}",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ListOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String?,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) scheme.primaryContainer.copy(alpha = 0.4f) else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(scheme.surfaceContainer),
        ) { Icon(icon, null, tint = scheme.onSurfaceVariant) }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
        }
        Icon(
            if (selected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
            null,
            tint = if (selected) scheme.primary else scheme.outline,
        )
    }
}

@Composable
private fun IngredientRow(row: CartRow, onToggle: () -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable(onClick = onToggle).padding(vertical = 2.dp),
    ) {
        Checkbox(checked = row.selected, onCheckedChange = { onToggle() })
        Column(Modifier.weight(1f).padding(top = 12.dp)) {
            Text(
                quantityText(row) + row.food,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (row.selected) scheme.onSurface else scheme.onSurfaceVariant,
            )
            if (row.note.isNotBlank()) {
                Text(row.note, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        }
    }
}

private fun quantityText(row: CartRow): String {
    val q = row.quantity ?: return ""
    val unit = row.unitName?.let { if (q > 1.0) row.unitPluralName ?: it else it }
    return formatQuantity(q) + (if (unit != null) " $unit" else "") + " "
}

/** 0.5 -> "½", 2.25 -> "2¼", 1.7 -> "1.7". */
internal fun formatQuantity(n: Double): String {
    val fractions = listOf(0.0 to "", 0.25 to "¼", 1.0 / 3 to "⅓", 0.5 to "½", 2.0 / 3 to "⅔", 0.75 to "¾", 1.0 to "")
    var whole = floor(n + 1e-9).toInt()
    val rest = n - whole
    val best = fractions.minBy { abs(it.first - rest) }
    if (abs(best.first - rest) > 0.06) return ((n * 10).roundToInt() / 10.0).toString().removeSuffix(".0")
    if (best.first == 1.0) whole += 1
    val sym = if (best.first == 1.0) "" else best.second
    return if (whole == 0) sym.ifEmpty { "0" } else "$whole$sym"
}
