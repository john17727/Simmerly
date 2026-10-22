package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemSectionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.LinkedRecipeUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.SectionKind
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ShoppingItemUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.DetailDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailSideEffect
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailState
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ConfirmDeleteListDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ListNameDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ListProgressBar
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.RecipeThumb
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ShoppingListDetailScreen(
    listId: String,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ShoppingListDetailViewModel = koinViewModel { parametersOf(listId) },
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            ShoppingListDetailSideEffect.NavigateBack -> onNavigateBack()
            is ShoppingListDetailSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message.asStringSuspend())
        }
    }

    Content(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@Composable
private fun Content(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var addFieldFocused by remember { mutableStateOf(false) }
    Scaffold(
        modifier = modifier,
        topBar = { TopBar(name = state.name, onNavigateBack = onNavigateBack, onEvent = onEvent) },
        bottomBar = {
            AddItemBar(
                text = state.addText,
                isAdding = state.isAdding,
                onTextChanged = { onEvent(ShoppingListDetailIntent.OnAddTextChanged(it)) },
                onSubmit = { onEvent(ShoppingListDetailIntent.OnAddItemSubmitted) },
                onFocusChanged = { addFieldFocused = it },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isLoading -> Unit
                state.name.isEmpty() && state.error != null -> Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.error?.asString().orEmpty(),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp),
                        )
                        OutlinedButton(
                            onClick = { onEvent(ShoppingListDetailIntent.OnRefresh) },
                            modifier = Modifier.padding(top = 16.dp),
                        ) { Text("Try again") }
                    }
                }

                else -> ListBody(state = state, onEvent = onEvent, contentPadding = padding)
            }
            if (addFieldFocused && state.addSuggestions.isNotEmpty()) {
                AddItemSuggestions(
                    suggestions = state.addSuggestions,
                    query = state.addSuggestionQuery,
                    highlightedId = state.addHighlightedId,
                    onPicked = { onEvent(ShoppingListDetailIntent.OnAddSuggestionPicked(it)) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(
                            start = 16.dp,
                            end = 16.dp,
                            bottom = padding.calculateBottomPadding() + 8.dp,
                        ),
                )
            }
        }
    }

    state.editor?.let { editor ->
        EditItemSheet(
            editor = editor,
            units = state.units,
            foodSuggestions = state.foodSuggestions,
            onEvent = onEvent,
        )
    }

    when (val dialog = state.dialog) {
        DetailDialog.None -> Unit
        is DetailDialog.Rename -> ListNameDialog(
            title = "Rename list",
            confirmLabel = "Save",
            initialName = dialog.currentName,
            onConfirm = { onEvent(ShoppingListDetailIntent.OnRenameConfirmed(it)) },
            onDismiss = { onEvent(ShoppingListDetailIntent.OnDialogDismissed) },
        )

        DetailDialog.ConfirmDelete -> ConfirmDeleteListDialog(
            name = state.name,
            onConfirm = { onEvent(ShoppingListDetailIntent.OnDeleteListConfirmed) },
            onDismiss = { onEvent(ShoppingListDetailIntent.OnDialogDismissed) },
        )
    }
}

@Composable
private fun TopBar(
    name: String,
    onNavigateBack: () -> Unit,
    onEvent: (ShoppingListDetailIntent) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            // Scaffold doesn't inset its topBar slot; a custom bar has to clear the status bar itself.
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 8.dp)
            .heightIn(min = 48.dp),
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to shopping lists")
        }
        Text(
            text = name,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Box {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(
                    Icons.Rounded.MoreVert,
                    contentDescription = "List options",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(
                    text = { Text("Rename") },
                    onClick = {
                        menuExpanded = false
                        onEvent(ShoppingListDetailIntent.OnRenameClicked)
                    },
                )
                DropdownMenuItem(
                    text = { Text("Delete list", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menuExpanded = false
                        onEvent(ShoppingListDetailIntent.OnDeleteListClicked)
                    },
                )
            }
        }
    }
}

@Composable
private fun ListBody(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        contentPadding = PaddingValues(
            top = contentPadding.calculateTopPadding(),
            bottom = contentPadding.calculateBottomPadding() + 24.dp,
        ),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "summary") {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            ) {
                Text(
                    text = progressSummary(state.remainingCount, state.checkedCount),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ListProgressBar(progress = state.progress)
            }
        }

        if (state.linkedRecipes.isNotEmpty()) {
            item(key = "recipes") {
                LinkedRecipesCard(
                    recipes = state.linkedRecipes,
                    onQuantityChanged = { recipeId, delta ->
                        onEvent(ShoppingListDetailIntent.OnRecipeQuantityChanged(recipeId, delta))
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }

        if (state.sections.isEmpty() && state.checkedItems.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "Nothing on this list yet. Add your first item below.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
                )
            }
            return@LazyColumn
        }

        item(key = "grouping") {
            GroupingToggle(
                selected = state.grouping,
                onSelected = { onEvent(ShoppingListDetailIntent.OnGroupingSelected(it)) },
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 4.dp),
            )
        }

        if (state.sections.isNotEmpty()) {
            item(key = "sections-control") {
                SectionsControl(
                    sectionCount = state.sections.size,
                    grouping = state.grouping,
                    anyExpanded = state.anySectionExpanded,
                    onToggleAll = { onEvent(ShoppingListDetailIntent.OnToggleAllSectionsClicked) },
                )
            }
        }

        state.sections.forEachIndexed { index, section ->
            section(
                section = section,
                isFirst = index == 0,
                collapsed = section.key in state.collapsedSections,
                onEvent = onEvent,
            )
        }

        if (state.checkedItems.isNotEmpty()) {
            item(key = "checked") {
                CheckedCard(
                    items = state.checkedItems,
                    expanded = state.checkedExpanded,
                    onEvent = onEvent,
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                )
            }
        }
    }
}

private fun LazyListScope.section(
    section: ItemSectionUi,
    isFirst: Boolean,
    collapsed: Boolean,
    onEvent: (ShoppingListDetailIntent) -> Unit,
) {
    item(key = "header-${section.key}") {
        val title = when (section.kind) {
            SectionKind.Aisle, SectionKind.Recipe -> section.title
            SectionKind.NoAisle -> "No aisle"
            SectionKind.AddedByHand -> "Added by hand"
        }
        Column(modifier = Modifier.animateItem()) {
            if (!isFirst) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                )
            }
            // The whole header row toggles the section, so the target is far larger than the icon.
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clickable(role = Role.Button) {
                        onEvent(ShoppingListDetailIntent.OnSectionToggled(section.key))
                    }
                    .semantics {
                        stateDescription = if (collapsed) "Collapsed" else "Expanded"
                    }
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
                Text(
                    text = section.items.size.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (collapsed) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                    // The row already reads as "<title>, <count>, Expanded/Collapsed".
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp),
                )
            }
        }
    }
    if (collapsed) return
    items(section.items, key = { "${section.key}-${it.id}" }) { item ->
        Column(modifier = Modifier.padding(start = 16.dp, end = 4.dp).animateItem()) {
            if (item != section.items.first()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            }
            ItemRow(
                item = item,
                onCheckedChange = { onEvent(ShoppingListDetailIntent.OnItemCheckedChanged(item.id, it)) },
                onEdit = { onEvent(ShoppingListDetailIntent.OnEditItemClicked(item.id)) },
            )
        }
    }
}

@Composable
private fun ItemRow(
    item: ShoppingItemUi,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .toggleable(value = item.checked, role = Role.Checkbox, onValueChange = onCheckedChange)
                .padding(vertical = 12.dp),
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(text = itemText(item), style = MaterialTheme.typography.bodyLarge)
                item.note?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                item.attribution?.let { Attribution(it) }
            }
        }
        IconButton(onClick = onEdit) {
            Icon(
                Icons.Rounded.Edit,
                contentDescription = "Edit ${item.name}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun Attribution(text: String, muted: Boolean = false) {
    val color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onTertiaryContainer
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

private fun itemText(item: ShoppingItemUi) = buildAnnotatedString {
    if (item.quantityText.isNotEmpty()) append("${item.quantityText} ")
    if (item.nameIsFood) {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(item.name) }
    } else {
        append(item.name)
    }
}

@Composable
private fun LinkedRecipesCard(
    recipes: List<LinkedRecipeUi>,
    onQuantityChanged: (String, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 8.dp)) {
            Text(
                text = "Linked recipes",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 4.dp),
            )
            recipes.forEachIndexed { index, recipe ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    RecipeThumb(
                        imageUrl = recipe.imageUrl,
                        contentDescription = "Recipe photo, ${recipe.name}",
                        size = 40.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recipe.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = recipeSummary(recipe),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    QuantityStepper(
                        quantity = recipe.quantity,
                        recipeName = recipe.name,
                        onDecrease = { onQuantityChanged(recipe.recipeId, -1) },
                        onIncrease = { onQuantityChanged(recipe.recipeId, 1) },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuantityStepper(
    quantity: Int,
    recipeName: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(44.dp)) {
            Icon(
                Icons.Rounded.Remove,
                contentDescription = if (quantity <= 1) "Remove $recipeName from this list"
                else "Fewer servings of $recipeName",
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 20.dp)
                .semantics { contentDescription = "Quantity $quantity" },
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = "More servings of $recipeName", modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
private fun SectionsControl(
    sectionCount: Int,
    grouping: ItemGrouping,
    anyExpanded: Boolean,
    onToggleAll: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
    ) {
        Text(
            text = when (grouping) {
                ItemGrouping.ByAisle -> if (sectionCount == 1) "1 aisle" else "$sectionCount aisles"
                ItemGrouping.ByRecipe -> if (sectionCount == 1) "1 section" else "$sectionCount sections"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = onToggleAll,
            contentPadding = PaddingValues(start = 8.dp, end = 12.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
        ) {
            Icon(
                if (anyExpanded) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (anyExpanded) "Collapse all" else "Expand all",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
private fun GroupingToggle(
    selected: ItemGrouping,
    onSelected: (ItemGrouping) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        ItemGrouping.entries.forEachIndexed { index, grouping ->
            SegmentedButton(
                selected = grouping == selected,
                onClick = { onSelected(grouping) },
                shape = SegmentedButtonDefaults.itemShape(index, ItemGrouping.entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                label = {
                    Text(
                        when (grouping) {
                            ItemGrouping.ByAisle -> "By aisle"
                            ItemGrouping.ByRecipe -> "By recipe"
                        }
                    )
                },
            )
        }
    }
}

@Composable
private fun CheckedCard(
    items: List<ShoppingItemUi>,
    expanded: Boolean,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            ) {
                Text(
                    text = "${items.size} checked",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onEvent(ShoppingListDetailIntent.OnUncheckAllClicked) }) {
                    Text(
                        "Uncheck all",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(onClick = { onEvent(ShoppingListDetailIntent.OnDeleteCheckedClicked) }) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete checked items",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = { onEvent(ShoppingListDetailIntent.OnCheckedExpandedToggled) }) {
                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Hide checked items" else "Show checked items",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    items.forEachIndexed { index, item ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        CheckedItemRow(
                            item = item,
                            onUncheck = { onEvent(ShoppingListDetailIntent.OnItemCheckedChanged(item.id, false)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CheckedItemRow(item: ShoppingItemUi, onUncheck: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = true, role = Role.Checkbox, onValueChange = { onUncheck() })
            .padding(vertical = 12.dp),
    ) {
        Checkbox(
            checked = true,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = listOf(item.quantityText, item.name).filter { it.isNotEmpty() }.joinToString(" "),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
            item.attribution?.let { Attribution(it, muted = true) }
        }
    }
}

private fun progressSummary(remaining: Int, checked: Int): String = when {
    remaining == 0 && checked == 0 -> "No items yet"
    remaining == 0 -> "All done · $checked checked"
    else -> "$remaining to get · $checked checked"
}

private fun recipeSummary(recipe: LinkedRecipeUi): String = buildString {
    append(if (recipe.itemCount == 1) "1 item" else "${recipe.itemCount} items")
    when {
        recipe.quantity == 2 -> append(" · doubled")
        recipe.quantity > 2 -> append(" · ×${recipe.quantity}")
    }
}

@Preview
@Composable
private fun ShoppingListDetailPreview() {
    fun item(id: String, qty: String, name: String, note: String? = null, from: String? = null, checked: Boolean = false) =
        ShoppingItemUi(id, qty, name, nameIsFood = true, note = note, attribution = from, checked = checked)
    SimmerlyTheme {
        Content(
            state = ShoppingListDetailState(
                isLoading = false,
                name = "Sunday brunch",
                remainingCount = 12,
                checkedCount = 3,
                progress = 0.2f,
                linkedRecipes = listOf(
                    LinkedRecipeUi("r1", "Shakshuka", "", 1, 6),
                    LinkedRecipeUi("r2", "Lemon ricotta pancakes", "", 2, 5),
                ),
                sections = listOf(
                    ItemSectionUi(
                        "produce", SectionKind.Aisle, "Produce", listOf(
                            item("1", "1", "yellow onion", "diced", "Shakshuka"),
                            item("2", "4", "lemons", "zested and juiced", "Lemon ricotta pancakes ×2"),
                            item("3", "1 pint", "strawberries"),
                        )
                    ),
                    ItemSectionUi("none", SectionKind.NoAisle, "", listOf(item("4", "2 tsp", "baking powder"))),
                ),
                checkedItems = listOf(item("5", "1 tsp", "ground cumin", from = "Shakshuka", checked = true)),
            ),
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
