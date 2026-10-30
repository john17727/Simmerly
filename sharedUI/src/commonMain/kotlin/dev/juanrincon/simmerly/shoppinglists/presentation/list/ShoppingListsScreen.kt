package dev.juanrincon.simmerly.shoppinglists.presentation.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ListCardFooterUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.RecipeThumbUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ShoppingListCardUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsSideEffect
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsState
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ConfirmDeleteListDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ListNameDialog
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ListProgressBar
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.RecipeAvatar
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.RecipeThumb
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun ShoppingListsScreen(
    onListSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
    selectedListId: String? = null,
    viewModel: ShoppingListsViewModel = koinViewModel(),
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is ShoppingListsSideEffect.OpenList -> onListSelected(sideEffect.listId)
            is ShoppingListsSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(sideEffect.message.asStringSuspend())
        }
    }

    if (isTwoPaneWidth()) {
        ShoppingListsExpanded(
            state = state,
            selectedListId = selectedListId,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
    } else {
        Content(
            state = state,
            onEvent = viewModel::onEvent,
            snackbarHostState = snackbarHostState,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: ShoppingListsState,
    onEvent: (ShoppingListsIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                isSearching = state.isSearching,
                query = state.query,
                scrollBehavior = scrollBehavior,
                onEvent = onEvent,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onEvent(ShoppingListsIntent.OnNewListClicked) },
                icon = { Icon(Icons.Rounded.Add, contentDescription = null) },
                text = { Text("New list", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            FilterRow(selected = state.filter, onEvent = onEvent)
            PullToRefreshBox(
                isRefreshing = state.isLoading && state.allLists != null,
                onRefresh = { onEvent(ShoppingListsIntent.OnRefresh) },
                modifier = Modifier.fillMaxSize(),
            ) {
                when {
                    state.allLists == null && state.error != null -> ErrorState(
                        message = state.error?.asString().orEmpty(),
                        onRetry = { onEvent(ShoppingListsIntent.OnRefresh) },
                    )

                    state.allLists == null -> LoadingState()

                    state.lists.isEmpty() -> EmptyState(state)

                    // One column on a phone; more as the window widens (an unfolded foldable).
                    else -> LazyVerticalGrid(
                        columns = GridCells.Adaptive(minSize = 320.dp),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 104.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.lists, key = { it.id }) { list ->
                            ShoppingListCard(
                                list = list,
                                onClick = { onEvent(ShoppingListsIntent.OnListClicked(list.id)) },
                                onRename = { onEvent(ShoppingListsIntent.OnRenameClicked(list.id)) },
                                onDelete = { onEvent(ShoppingListsIntent.OnDeleteClicked(list.id)) },
                                modifier = Modifier.fillMaxHeight().animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }

    ListDialogs(state = state, onEvent = onEvent)
}

/** The create, rename and delete dialogs, shared by the phone and desktop layouts. */
@Composable
internal fun ListDialogs(state: ShoppingListsState, onEvent: (ShoppingListsIntent) -> Unit) {
    when (val dialog = state.dialog) {
        ListDialog.None -> Unit
        ListDialog.Create -> ListNameDialog(
            title = "New list",
            confirmLabel = "Create",
            initialName = "",
            onConfirm = { onEvent(ShoppingListsIntent.OnCreateConfirmed(it)) },
            onDismiss = { onEvent(ShoppingListsIntent.OnDialogDismissed) },
        )

        is ListDialog.Rename -> ListNameDialog(
            title = "Rename list",
            confirmLabel = "Save",
            initialName = dialog.currentName,
            onConfirm = { onEvent(ShoppingListsIntent.OnRenameConfirmed(it)) },
            onDismiss = { onEvent(ShoppingListsIntent.OnDialogDismissed) },
        )

        is ListDialog.ConfirmDelete -> ConfirmDeleteListDialog(
            name = dialog.name.asString(),
            onConfirm = { onEvent(ShoppingListsIntent.OnDeleteConfirmed) },
            onDismiss = { onEvent(ShoppingListsIntent.OnDialogDismissed) },
        )
    }
}

/**
 * "Shopping lists" as a large title that collapses into the bar on scroll, like Recipes. Searching
 * swaps it for a regular bar holding the search field.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun TopBar(
    isSearching: Boolean,
    query: String,
    scrollBehavior: TopAppBarScrollBehavior,
    onEvent: (ShoppingListsIntent) -> Unit,
) {
    if (isSearching) {
        val focusRequester = remember { FocusRequester() }
        LaunchedEffect(Unit) { focusRequester.requestFocus() }
        TopAppBar(
            title = {
                TextField(
                    value = query,
                    onValueChange = { onEvent(ShoppingListsIntent.OnQueryChanged(it)) },
                    placeholder = { Text("Search lists") },
                    singleLine = true,
                    shape = CircleShape,
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
            },
            actions = {
                IconButton(onClick = { onEvent(ShoppingListsIntent.OnSearchToggled) }) {
                    Icon(Icons.Rounded.Close, contentDescription = "Close search")
                }
            },
            scrollBehavior = scrollBehavior,
        )
    } else {
        LargeFlexibleTopAppBar(
            title = { Text("Shopping lists") },
            actions = {
                IconButton(onClick = { onEvent(ShoppingListsIntent.OnSearchToggled) }) {
                    Icon(
                        Icons.Rounded.Search,
                        contentDescription = "Search lists",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            scrollBehavior = scrollBehavior,
        )
    }
}

@Composable
private fun FilterRow(selected: ListFilter, onEvent: (ShoppingListsIntent) -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 16.dp),
    ) {
        FilterChips(selected = selected, onEvent = onEvent)
    }
}

/** The Mine / Household chips. */
@Composable
internal fun FilterChips(selected: ListFilter, onEvent: (ShoppingListsIntent) -> Unit) {
    ListFilter.entries.forEach { filter ->
        ListFilterChip(
            label = when (filter) {
                ListFilter.Mine -> "Mine"
                ListFilter.Household -> "Household"
            },
            selected = filter == selected,
            onClick = { onEvent(ShoppingListsIntent.OnFilterSelected(filter)) },
        )
    }
}

/**
 * A filter chip drawn to the design rather than with Material's `FilterChip`, whose hover state
 * layer renders on desktop as a heavy grey wash with a pale box left behind the label. Hover and
 * press only tint the chip's own background, like the design's ghost buttons.
 */
@Composable
private fun ListFilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val container = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        pressed -> MaterialTheme.colorScheme.surfaceContainerHigh
        hovered -> MaterialTheme.colorScheme.surfaceContainer
        else -> Color.Transparent
    }
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(8.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .heightIn(min = 32.dp)
            .clip(shape)
            .background(container, shape)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
            )
            .hoverable(interactionSource)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = Role.Tab,
                onClick = onClick,
            )
            .padding(start = if (selected) 8.dp else 16.dp, end = 16.dp),
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}

@Composable
private fun ShoppingListCard(
    list: ShoppingListCardUi,
    onClick: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedCard(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = list.name.asString(),
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = itemSummary(list.itemCount, list.checkedCount),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                ListMenu(
                    listName = list.name.asString(),
                    onRename = onRename,
                    onDelete = onDelete,
                    modifier = Modifier.offset(x = 12.dp, y = (-12).dp),
                )
            }
            ListProgressBar(progress = list.progress)
            if (list.footer != ListCardFooterUi.Empty) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                CardFooter(list.footer)
            }
        }
    }
}

@Composable
private fun ListMenu(
    listName: String,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }) {
            Icon(
                Icons.Rounded.MoreVert,
                contentDescription = "More options for $listName",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Rename") },
                onClick = { expanded = false; onRename() },
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                onClick = { expanded = false; onDelete() },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardFooter(footer: ListCardFooterUi) {
    when (footer) {
        ListCardFooterUi.Empty -> Unit

        is ListCardFooterUi.OneRecipe -> FooterRow(
            leading = {
                RecipeThumb(
                    imageUrl = footer.recipe.imageUrl,
                    contentDescription = "Recipe photo, ${footer.recipe.name.asString()}",
                    size = 48.dp,
                )
            },
            title = footer.recipe.name.asString(),
            subtitle = if (footer.itemCount == 1) "1 item from this recipe"
            else "All ${footer.itemCount} items from this recipe",
            trailing = { QuantityPill(footer.quantity) },
        )

        is ListCardFooterUi.SeveralRecipes -> FooterRow(
            leading = { AvatarStack(footer.recipes, footer.recipeCount) },
            title = "${footer.recipeCount} recipes",
            subtitle = footer.recipeNames.map { it.asString() }.joinToString(),
        )

        is ListCardFooterUi.AddedByHand -> FooterRow(
            leading = {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(MaterialTheme.shapes.small)
                        .background(MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Icon(
                        Icons.Rounded.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            },
            title = "Added by hand",
            subtitle = buildString {
                append(footer.itemNames.map { it.asString() }.joinToString())
                val more = footer.itemCount - footer.itemNames.size
                if (more > 0) append(", +$more")
            },
        )

        is ListCardFooterUi.RecipesAndExtras -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            FooterRow(
                leading = { AvatarStack(footer.recipes, footer.recipeCount) },
                title = buildString {
                    append(if (footer.recipeCount == 1) "1 recipe" else "${footer.recipeCount} recipes")
                    append(" and ")
                    append(if (footer.byHandCount == 1) "1 extra" else "${footer.byHandCount} extras")
                },
                subtitle = footer.recipeNames.map { it.asString() }.joinToString(),
            )
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SourceChip(
                    text = "${footer.fromRecipesCount} from recipes",
                    icon = { Icon(Icons.Rounded.Restaurant, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    container = MaterialTheme.colorScheme.tertiaryContainer,
                    content = MaterialTheme.colorScheme.onTertiaryContainer,
                    border = MaterialTheme.colorScheme.tertiary,
                )
                SourceChip(
                    text = "${footer.byHandCount} added by hand",
                    icon = { Icon(Icons.Rounded.EditNote, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    container = MaterialTheme.colorScheme.surface,
                    content = MaterialTheme.colorScheme.onSurfaceVariant,
                    border = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
        }
    }
}

@Composable
private fun FooterRow(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        leading()
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        trailing?.invoke()
    }
}

@Composable
private fun QuantityPill(quantity: Int) {
    Text(
        text = "×$quantity",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier
            .semantics { contentDescription = "Quantity $quantity" }
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = 8.dp, vertical = 4.dp),
    )
}

/** Overlapping recipe avatars, ending in a "+N" bubble when there are more recipes than shown. */
@Composable
private fun AvatarStack(recipes: List<RecipeThumbUi>, recipeCount: Int) {
    val ring = MaterialTheme.colorScheme.surfaceContainerLow
    Row(horizontalArrangement = Arrangement.spacedBy((-10).dp)) {
        recipes.forEach { recipe ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(36.dp).clip(CircleShape).background(ring),
            ) {
                RecipeAvatar(recipe.imageUrl)
            }
        }
        val overflow = recipeCount - recipes.size
        if (overflow > 0) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .border(2.dp, ring, CircleShape)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            ) {
                Text(
                    text = "+$overflow",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                )
            }
        }
    }
}

@Composable
private fun SourceChip(
    text: String,
    icon: @Composable () -> Unit,
    container: Color,
    content: Color,
    border: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .height(28.dp)
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .border(1.dp, border, MaterialTheme.shapes.small)
            .padding(start = 8.dp, end = 10.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            icon()
            Text(text = text, style = MaterialTheme.typography.labelMedium, color = content)
        }
    }
}

@Composable
internal fun LoadingState() {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.surfaceContainerHighest,
        MaterialTheme.colorScheme.surfaceContainer,
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .shimmer(shimmerColors, shape = RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
internal fun EmptyState(state: ShoppingListsState) {
    CenteredMessage(
        title = when {
            state.query.isNotBlank() -> "No lists match “${state.query.trim()}”"
            state.filter == ListFilter.Mine -> "You have no shopping lists yet"
            else -> "Your household has no shopping lists yet"
        },
        body = if (state.query.isNotBlank()) null else "Start one with New list, then add items by hand.",
    )
}

@Composable
internal fun ErrorState(message: String, onRetry: () -> Unit) {
    CenteredMessage(title = message, body = null) {
        OutlinedButton(onClick = onRetry) { Text("Try again") }
    }
}

@Composable
private fun CenteredMessage(
    title: String,
    body: String?,
    action: (@Composable () -> Unit)? = null,
) {
    // Scrollable so pull-to-refresh still works on an empty screen.
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(horizontal = 32.dp, vertical = 64.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Icon(
                Icons.Rounded.ShoppingCart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(48.dp),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
            )
            if (body != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
            if (action != null) {
                Spacer(Modifier.height(16.dp))
                action()
            }
        }
    }
}

private fun itemSummary(itemCount: Int, checkedCount: Int): String {
    val items = if (itemCount == 1) "1 item" else "$itemCount items"
    val checked = when {
        itemCount == 0 -> return "No items yet"
        checkedCount == 0 -> "none checked"
        checkedCount == itemCount -> "all checked"
        else -> "$checkedCount checked"
    }
    return "$items · $checked"
}

@Preview
@Composable
private fun ShoppingListsPreview() {
    SimmerlyTheme {
        Content(
            state = ShoppingListsState(
                allLists = previewLists,
                lists = previewLists,
            ),
            onEvent = {},
            snackbarHostState = remember { SnackbarHostState() },
        )
    }
}

private val previewLists = run {
    val recipe = RecipeThumbUi("r1", UiText.Dynamic("Beef and broccoli"), "")
    fun names(vararg names: String) = names.map { UiText.Dynamic(it) }
    listOf(
        ShoppingListCardUi(
            "1", UiText.Dynamic("Tonight's dinner"), "u", 11, 1, 1 / 11f,
            ListCardFooterUi.OneRecipe(recipe, 11, 1),
        ),
        ShoppingListCardUi(
            "2", UiText.Dynamic("Week of Oct 26"), "u", 34, 0, 0f,
            ListCardFooterUi.SeveralRecipes(
                List(3) { recipe.copy(recipeId = "r$it") }, 4,
                names("Beef and broccoli", "Shakshuka", "Pad see ew", "Miso salmon"),
            ),
        ),
        ShoppingListCardUi(
            "3", UiText.Dynamic("Costco run"), "u", 9, 6, 6 / 9f,
            ListCardFooterUi.AddedByHand(names("Paper towels", "olive oil", "coffee beans"), 9),
        ),
        ShoppingListCardUi(
            "4", UiText.Dynamic("Sunday brunch"), "u", 15, 3, 0.2f,
            ListCardFooterUi.RecipesAndExtras(
                List(2) { recipe.copy(recipeId = "r$it") }, 2,
                names("Shakshuka", "Lemon ricotta pancakes"), 10, 5,
            ),
        ),
    )
}
