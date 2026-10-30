package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
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
import kotlinx.coroutines.launch
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

            is ShoppingListDetailSideEffect.ShowCheckedAll -> {
                val result = snackbarHostState.showSnackbar(
                    message = if (sideEffect.count == 1) "1 item checked" else "${sideEffect.count} items checked",
                    actionLabel = "Undo",
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.onEvent(ShoppingListDetailIntent.OnUndoCheckAllClicked)
                }
            }
        }
    }

    if (isTwoPaneWidth()) {
        ShoppingListDetailExpanded(
            state = state,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
        ListDetailDialogs(state = state, onEvent = viewModel::onEvent)
    } else {
        Content(
            state = state,
            onEvent = viewModel::onEvent,
            onNavigateBack = onNavigateBack,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    var addFieldFocused by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    // Long lists only: the button appears once the summary has scrolled out of view.
    val showScrollToTop by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                name = state.name?.asString().orEmpty(),
                scrollBehavior = scrollBehavior,
                onNavigateBack = onNavigateBack,
                onEvent = onEvent,
            )
        },
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
                state.name == null && state.error != null -> Box(
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

                else -> ListBody(
                    state = state,
                    onEvent = onEvent,
                    listState = listState,
                    contentPadding = padding,
                )
            }
            ScrollToTopButton(
                // Hidden while the suggestions card has the space above the add bar.
                visible = showScrollToTop && !(addFieldFocused && state.addSuggestions.isNotEmpty()),
                // Sits above the snackbar while one is showing, as in the design.
                liftedForSnackbar = snackbarHostState.currentSnackbarData != null,
                onClick = {
                    coroutineScope.launch {
                        listState.animateScrollToItem(0)
                        // A programmatic scroll doesn't reach the bar's nested scroll, so bring
                        // the large title back explicitly.
                        scrollBehavior.state.heightOffset = 0f
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = padding.calculateBottomPadding() + 16.dp),
            )
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

    ListDetailDialogs(state = state, onEvent = onEvent)
}

/** The rename and delete dialogs, shared by the phone and desktop layouts. */
@Composable
private fun ListDetailDialogs(state: ShoppingListDetailState, onEvent: (ShoppingListDetailIntent) -> Unit) {
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
            name = state.name?.asString().orEmpty(),
            onConfirm = { onEvent(ShoppingListDetailIntent.OnDeleteListConfirmed) },
            onDismiss = { onEvent(ShoppingListDetailIntent.OnDialogDismissed) },
        )
    }
}

/** The list's name as a large title that collapses into the bar on scroll, like Recipes. */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(
    name: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateBack: () -> Unit,
    onEvent: (ShoppingListDetailIntent) -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    LargeFlexibleTopAppBar(
        title = { Text(text = name, maxLines = 1, overflow = TextOverflow.Ellipsis) },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back to shopping lists")
            }
        },
        actions = {
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
        },
        scrollBehavior = scrollBehavior,
    )
}

@Composable
private fun ListBody(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    listState: LazyListState,
    contentPadding: PaddingValues,
) {
    LazyColumn(
        state = listState,
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
                    onCheckAll = { onEvent(ShoppingListDetailIntent.OnCheckAllClicked) },
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
            SectionKind.Aisle, SectionKind.Recipe -> section.title.asString()
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
                Text(
                    text = itemText(item.quantityText.asString(), item.name.asString(), item.nameIsFood),
                    style = MaterialTheme.typography.bodyLarge,
                )
                item.note?.let {
                    Text(
                        text = it.asString(),
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
                contentDescription = "Edit ${item.name.asString()}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
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
                        contentDescription = "Recipe photo, ${recipe.name.asString()}",
                        size = 40.dp,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = recipe.name.asString(),
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
                        recipeName = recipe.name.asString(),
                        onDecrease = { onQuantityChanged(recipe.recipeId, -1) },
                        onIncrease = { onQuantityChanged(recipe.recipeId, 1) },
                    )
                }
            }
        }
    }
}

/** The small outlined button that jumps back to the top of a long list. */
@Composable
private fun ScrollToTopButton(
    visible: Boolean,
    liftedForSnackbar: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // A snackbar is 48dp tall with a 12dp gap above it.
    val lift by animateDpAsState(if (liftedForSnackbar) 60.dp else 0.dp)
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        modifier = modifier.offset { IntOffset(0, -lift.roundToPx()) },
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 6.dp,
            modifier = Modifier.size(48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Rounded.ArrowUpward, contentDescription = "Scroll to top")
            }
        }
    }
}

@Preview
@Composable
private fun ShoppingListDetailPreview() {
    fun text(value: String) = UiText.Dynamic(value)
    fun item(id: String, qty: String, name: String, note: String? = null, from: String? = null, checked: Boolean = false) =
        ShoppingItemUi(
            id = id,
            quantityText = text(qty),
            name = text(name),
            nameIsFood = true,
            note = note?.let(::text),
            attribution = from?.let(::text),
            checked = checked,
        )
    SimmerlyTheme {
        Content(
            state = ShoppingListDetailState(
                isLoading = false,
                name = text("Sunday brunch"),
                remainingCount = 12,
                checkedCount = 3,
                progress = 0.2f,
                linkedRecipes = listOf(
                    LinkedRecipeUi("r1", text("Shakshuka"), "", 1, 6),
                    LinkedRecipeUi("r2", text("Lemon ricotta pancakes"), "", 2, 5),
                ),
                sections = listOf(
                    ItemSectionUi(
                        "produce", SectionKind.Aisle, text("Produce"), listOf(
                            item("1", "1", "yellow onion", "diced", "Shakshuka"),
                            item("2", "4", "lemons", "zested and juiced", "Lemon ricotta pancakes ×2"),
                            item("3", "1 pint", "strawberries"),
                        )
                    ),
                    ItemSectionUi("none", SectionKind.NoAisle, text(""), listOf(item("4", "2 tsp", "baking powder"))),
                ),
                checkedItems = listOf(item("5", "1 tsp", "ground cumin", from = "Shakshuka", checked = true)),
            ),
            onEvent = {},
            onNavigateBack = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}
