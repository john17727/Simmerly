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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Surface
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.MenuBook
import androidx.compose.material.icons.rounded.RadioButtonChecked
import androidx.compose.material.icons.rounded.RadioButtonUnchecked
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import dev.juanrincon.simmerly.core.presentation.StepperPill
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.presentation.orbit.AddTab
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddIntent
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddSideEffect
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddState
import kotlinx.datetime.LocalDate
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

private enum class AddView { Form, Date, Type }

/** "Plan a meal" / edit: a bottom sheet with the form and two sub-views (date, meal). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MealPlanAddSheet(
    args: MealPlanAddArgs,
    plannedDays: Set<LocalDate>,
    onDismiss: () -> Unit,
    onSaved: (MealPlanEntry) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MealPlanAddViewModel = koinViewModel(key = "meal-plan-add-${args}") { parametersOf(args) },
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is MealPlanAddSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.asStringSuspend())
            is MealPlanAddSideEffect.Saved -> onSaved(effect.entry)
        }
    }
    when {
        // Desktop: a centred two-column dialog.
        isTwoPaneWidth() -> Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(880.dp).heightIn(max = 720.dp),
            ) {
                Box {
                    MealPlanAddWide(state, viewModel::onEvent, plannedDays, onDismiss)
                    SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
                }
            }
        }
        // Unfolded foldable: a wide bottom sheet with the same two columns.
        isMediumWidthOrWider() -> ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            sheetMaxWidth = 880.dp,
            modifier = modifier,
        ) {
            Box(Modifier.fillMaxHeight(0.94f)) {
                MealPlanAddWide(state, viewModel::onEvent, plannedDays, onDismiss)
                SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
            }
        }
        else -> ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            modifier = modifier,
        ) {
            Box(Modifier.fillMaxHeight(0.94f)) {
                MealPlanAddContent(state, viewModel::onEvent, plannedDays, onDismiss)
                SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 80.dp))
            }
        }
    }
}

@Composable
internal fun MealPlanAddContent(
    state: MealPlanAddState,
    onEvent: (MealPlanAddIntent) -> Unit,
    plannedDays: Set<LocalDate>,
    onDismiss: () -> Unit,
) {
    var view by remember { mutableStateOf(AddView.Form) }
    when (view) {
        AddView.Form -> AddForm(
            state, onEvent, onDismiss,
            onPickDate = { view = AddView.Date },
            onPickType = { view = AddView.Type },
        )
        AddView.Date -> AddDateView(state, onEvent, plannedDays, onBack = { view = AddView.Form })
        AddView.Type -> AddTypeView(state, onEvent, onBack = { view = AddView.Form })
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun AddForm(
    state: MealPlanAddState,
    onEvent: (MealPlanAddIntent) -> Unit,
    onDismiss: () -> Unit,
    onPickDate: () -> Unit,
    onPickType: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth().imePadding()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 24.dp, end = 8.dp)) {
            Text(
                if (state.isEdit) "Edit meal" else "Plan a meal",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onDismiss) { Icon(Icons.Rounded.Close, contentDescription = "Close") }
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp, vertical = 8.dp),
        ) {
            val shape = RoundedCornerShape(12.dp)
            Column(
                Modifier.fillMaxWidth().clip(shape).background(scheme.surfaceContainerLow)
                    .border(1.dp, scheme.outlineVariant, shape)
            ) {
                PickerRow(
                    icon = Icons.Rounded.CalendarToday,
                    label = if (state.wholeWeek) "Every day, whole week" else "Date",
                    value = if (state.wholeWeek) {
                        val days = state.date.weekDays(state.firstDay)
                        weekRangeLabel(days.first(), days.last())
                    } else (if (state.date == state.today) "Today, " else "") + state.date.shortDayLabel(),
                    onClick = onPickDate,
                )
                HorizontalDivider(color = scheme.outlineVariant, modifier = Modifier.padding(start = 54.dp))
                PickerRow(icon = state.type.icon(), label = "Meal", value = state.type.label(), onClick = onPickType)
            }

            if (!state.recipeLocked) {
                Row(horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)) {
                    ToggleButton(
                        checked = state.tab == AddTab.Recipe,
                        onCheckedChange = { onEvent(MealPlanAddIntent.SetTab(AddTab.Recipe)) },
                        shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.MenuBook, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Recipe")
                    }
                    ToggleButton(
                        checked = state.tab == AddTab.Note,
                        onCheckedChange = { onEvent(MealPlanAddIntent.SetTab(AddTab.Note)) },
                        shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(Icons.Rounded.StickyNote2, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.size(8.dp))
                        Text("Note")
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
        HorizontalDivider(color = scheme.outlineVariant)
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(),
        ) {
            TextButton(onClick = onDismiss) { Text("Cancel") }
            Button(onClick = { onEvent(MealPlanAddIntent.Save) }, enabled = state.canSave) {
                Text(
                    when {
                        !state.canSave && !state.isSaving ->
                            if (state.tab == AddTab.Recipe) "Choose a recipe" else "Add a title"
                        state.isEdit -> "Save"
                        state.wholeWeek -> "Add to ${state.type.label().lowercase()}, 7 days"
                        else -> "Add to ${state.type.label().lowercase()}"
                    }
                )
            }
        }
    }
}

@Composable
private fun PickerRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    onClick: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable(onClick = onClick)
            .padding(start = 16.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
    ) {
        Icon(icon, null, tint = scheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
        }
        Icon(Icons.Rounded.ChevronRight, null, tint = scheme.onSurfaceVariant)
    }
}

@Composable
internal fun RecipePicker(state: MealPlanAddState, onEvent: (MealPlanAddIntent) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        OutlinedTextField(
            value = state.query,
            onValueChange = { onEvent(MealPlanAddIntent.SetQuery(it)) },
            placeholder = { Text("Search recipes") },
            leadingIcon = { Icon(Icons.Rounded.Search, null) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.horizontalScroll(rememberScrollState()),
        ) {
            FilterDropdownChip("Categories", state, isTag = false, onEvent)
            FilterDropdownChip("Tags", state, isTag = true, onEvent)
            FilterChip(
                selected = state.rulesEnabled,
                enabled = state.rulesAvailable,
                onClick = { onEvent(MealPlanAddIntent.SetRulesEnabled(!state.rulesEnabled)) },
                label = { Text("Planner rules") },
                leadingIcon = if (state.rulesEnabled) {
                    { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(18.dp)) }
                } else null,
            )
        }
        val recipes = state.recipes
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            recipes.forEach { item ->
                val selected = item.recipe.id == state.selectedRecipeId
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) scheme.primaryContainer.copy(alpha = 0.5f) else androidx.compose.ui.graphics.Color.Transparent)
                        .clickable { onEvent(MealPlanAddIntent.SelectRecipe(item.recipe.id)) }
                        .padding(start = 8.dp, end = 12.dp, top = 8.dp, bottom = 8.dp),
                ) {
                    SubcomposeAsyncImage(
                        model = item.recipe.image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(scheme.surfaceContainer),
                    )
                    Column(Modifier.weight(1f)) {
                        Text(
                            item.recipe.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            item.meta.label(),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                    Icon(
                        if (selected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (selected) scheme.primary else scheme.outline,
                    )
                }
            }
            if (recipes.isEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                ) {
                    Icon(Icons.Rounded.SearchOff, null, tint = scheme.outline, modifier = Modifier.size(32.dp))
                    Text(
                        if (state.query.isBlank()) "No recipes match these filters." else "No recipes match “${state.query}”.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = scheme.onSurfaceVariant,
                    )
                    TextButton(onClick = { onEvent(MealPlanAddIntent.SetTab(AddTab.Note)) }) {
                        Text("Add it as a note instead")
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterDropdownChip(
    label: String,
    state: MealPlanAddState,
    isTag: Boolean,
    onEvent: (MealPlanAddIntent) -> Unit,
) {
    var open by remember { mutableStateOf(false) }
    val options = state.filters.filter { it.isTag == isTag }
    val picked = options.count { it.id in state.selectedFilterIds }
    Box {
        FilterChip(
            selected = picked > 0,
            enabled = options.isNotEmpty(),
            onClick = { open = true },
            label = { Text(if (picked > 0) "${label} · ${picked}" else label) },
        )
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    leadingIcon = {
                        if (option.id in state.selectedFilterIds) Icon(Icons.Rounded.Check, null)
                    },
                    onClick = { onEvent(MealPlanAddIntent.ToggleFilter(option.id)) },
                )
            }
        }
    }
}

@Composable
internal fun LockedRecipe(state: MealPlanAddState) {
    val recipe = state.allRecipes.firstOrNull { it.id == state.selectedRecipeId } ?: return
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        SubcomposeAsyncImage(
            model = recipe.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(48.dp).clip(RoundedCornerShape(8.dp)).background(MaterialTheme.colorScheme.surfaceContainer),
        )
        Text(recipe.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
}

@Composable
internal fun RecipeExtras(state: MealPlanAddState, onEvent: (MealPlanAddIntent) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val servings = state.servings
    if (state.selectedRecipeId != null && servings != null) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Servings", style = MaterialTheme.typography.labelLarge, modifier = Modifier.weight(1f))
            StepperPill(
                label = servingsLabel(servings),
                onDecrease = { onEvent(MealPlanAddIntent.ChangeServings(-1)) },
                onIncrease = { onEvent(MealPlanAddIntent.ChangeServings(1)) },
                decreaseDescription = "Fewer servings",
                increaseDescription = "More servings",
                canDecrease = servings > 1.0,
            )
        }
    }
    if (state.isDuplicate) {
        Row(
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp))
                .background(scheme.tertiaryContainer).padding(12.dp),
        ) {
            Icon(Icons.Rounded.Warning, null, tint = scheme.onTertiaryContainer, modifier = Modifier.size(20.dp))
            Text(
                "Already planned for ${state.type.label().lowercase()}" +
                    if (state.wholeWeek) " this week." else " on this day.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
internal fun NoteFields(state: MealPlanAddState, onEvent: (MealPlanAddIntent) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        OutlinedTextField(
            value = state.noteTitle,
            onValueChange = { onEvent(MealPlanAddIntent.SetNoteTitle(it)) },
            label = { Text("Title") },
            placeholder = { Text("Leftovers, eating out…") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.noteText,
            onValueChange = { onEvent(MealPlanAddIntent.SetNoteText(it)) },
            label = { Text("Note (optional)") },
            placeholder = { Text("Anything to remember") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth(),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Text(
                "Notes show up in the plan like a recipe, without ingredients.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun AddDateView(
    state: MealPlanAddState,
    onEvent: (MealPlanAddIntent) -> Unit,
    plannedDays: Set<LocalDate>,
    onBack: () -> Unit,
) {
    var month by remember { mutableStateOf(state.date) }
    val days = state.date.weekDays(state.firstDay)
    Column(Modifier.fillMaxWidth()) {
        SubHeader("Choose a date", onBack)
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp),
        ) {
            MonthCalendar(
                month = month,
                firstDay = state.firstDay,
                selected = state.date,
                today = state.today,
                plannedDays = plannedDays,
                onSelect = { onEvent(MealPlanAddIntent.SetDate(it)); month = it },
                onMonthChange = { month = it },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(Modifier.weight(1f)) {
                    Text("Plan for the whole week", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                    Text(
                        weekRangeLabel(days.first(), days.last()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.wholeWeek,
                    onCheckedChange = { onEvent(MealPlanAddIntent.SetWholeWeek(it)) },
                    thumbContent = if (state.wholeWeek) {
                        { Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp)) }
                    } else null,
                )
            }
        }
        DoneBar(onBack)
    }
}

@Composable
private fun AddTypeView(
    state: MealPlanAddState,
    onEvent: (MealPlanAddIntent) -> Unit,
    onBack: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    Column(Modifier.fillMaxWidth()) {
        SubHeader("Choose a meal", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 16.dp)) {
            Text(
                if (state.wholeWeek) "Any day this week" else state.date.dayTitle(),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            )
            MealType.entries.forEach { type ->
                val count = state.weekEntries.count { e ->
                    e.type == type && e.id != state.editingId &&
                        if (state.wholeWeek) e.isWholeWeek else !e.isWholeWeek && e.date == state.date
                }
                val selected = type == state.type
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onEvent(MealPlanAddIntent.SetType(type)); onBack() }
                        .padding(horizontal = 8.dp, vertical = 8.dp),
                ) {
                    Icon(type.icon(), null, tint = scheme.onSurfaceVariant)
                    Column(Modifier.weight(1f)) {
                        Text(type.label(), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                        Text(
                            if (count == 0) "Nothing planned" else "${count} planned",
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                    Icon(
                        if (selected) Icons.Rounded.RadioButtonChecked else Icons.Rounded.RadioButtonUnchecked,
                        null,
                        tint = if (selected) scheme.primary else scheme.outline,
                    )
                }
            }
        }
    }
}

@Composable
private fun SubHeader(title: String, onBack: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(start = 4.dp, end = 16.dp, bottom = 8.dp)) {
        IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back") }
        Text(title, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DoneBar(onDone: () -> Unit) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp).navigationBarsPadding(),
        ) {
            Button(onClick = onDone) { Text("Done") }
        }
    }
}
