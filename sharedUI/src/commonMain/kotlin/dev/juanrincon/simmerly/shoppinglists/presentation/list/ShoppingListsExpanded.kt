package dev.juanrincon.simmerly.shoppinglists.presentation.list

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ListCardFooterUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.RecipeThumbUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ShoppingListCardUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ShoppingListsState
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ListProgressBar
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/**
 * The lists pane on wide windows, from the "Shopping lists, desktop" artboard: a static title
 * with search, the filter chips beside a "New list" button, and compact cards. The open list's
 * card is outlined in coral; rename and delete live in that list's menu in the detail pane.
 */
@Composable
internal fun ShoppingListsExpanded(
    state: ShoppingListsState,
    selectedListId: String?,
    onEvent: (ShoppingListsIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize().padding(start = 16.dp, end = 16.dp, top = 16.dp),
        ) {
            PaneHeader(isSearching = state.isSearching, query = state.query, onEvent = onEvent)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChips(selected = state.filter, onEvent = onEvent)
                Spacer(Modifier.weight(1f))
                FilledTonalButton(
                    onClick = { onEvent(ShoppingListsIntent.OnNewListClicked) },
                    contentPadding = PaddingValues(start = 12.dp, end = 16.dp),
                ) {
                    Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text("New list", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(start = 8.dp))
                }
            }
            Box(modifier = Modifier.weight(1f)) {
                when {
                    state.allLists == null && state.error != null -> ErrorState(
                        message = state.error?.asString().orEmpty(),
                        onRetry = { onEvent(ShoppingListsIntent.OnRefresh) },
                    )

                    state.allLists == null -> LoadingState()

                    state.lists.isEmpty() -> EmptyState(state)

                    else -> LazyColumn(
                        contentPadding = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        items(state.lists, key = { it.id }) { list ->
                            CompactListCard(
                                list = list,
                                selected = list.id == selectedListId,
                                onClick = { onEvent(ShoppingListsIntent.OnListClicked(list.id)) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                    }
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    ListDialogs(state = state, onEvent = onEvent)
}

@Composable
private fun PaneHeader(
    isSearching: Boolean,
    query: String,
    onEvent: (ShoppingListsIntent) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp),
    ) {
        if (isSearching) {
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
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
                modifier = Modifier.weight(1f).focusRequester(focusRequester),
            )
            IconButton(onClick = { onEvent(ShoppingListsIntent.OnSearchToggled) }) {
                Icon(Icons.Rounded.Close, contentDescription = "Close search")
            }
        } else {
            Text(
                text = "Shopping lists",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = { onEvent(ShoppingListsIntent.OnSearchToggled) }) {
                Icon(
                    Icons.Rounded.Search,
                    contentDescription = "Search lists",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun CompactListCard(
    list: ShoppingListCardUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(12.dp),
        color = when {
            selected -> MaterialTheme.colorScheme.surface
            hovered -> MaterialTheme.colorScheme.surfaceContainer
            else -> MaterialTheme.colorScheme.surfaceContainerLow
        },
        border = if (selected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth().hoverable(interactionSource),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            // The selected card's thicker border takes the extra pixel from the padding, so the
            // content doesn't shift when the selection moves.
            modifier = Modifier.padding(
                horizontal = if (selected) 15.dp else 16.dp,
                vertical = if (selected) 13.dp else 14.dp,
            ),
        ) {
            Row(verticalAlignment = Alignment.Top, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = list.name.asString(),
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "${list.checkedCount} of ${list.itemCount}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            ListProgressBar(progress = list.progress)
            CompactFooter(list.footer)
        }
    }
}

/** The one-line summary of where a list's items came from. */
@Composable
private fun CompactFooter(footer: ListCardFooterUi) {
    val (icon, text) = when (footer) {
        ListCardFooterUi.Empty -> Icons.Rounded.EditNote to "No items yet"
        is ListCardFooterUi.OneRecipe -> Icons.Rounded.Restaurant to footer.recipe.name.asString()
        is ListCardFooterUi.SeveralRecipes -> {
            val names = footer.recipeNames.map { it.asString() }
            val shown = names.take(2).joinToString()
            val more = names.size - 2
            Icons.Rounded.Restaurant to buildString {
                append("${footer.recipeCount} recipes · $shown")
                if (more > 0) append(", +$more")
            }
        }

        is ListCardFooterUi.AddedByHand -> Icons.Rounded.EditNote to "Added by hand"
        is ListCardFooterUi.RecipesAndExtras -> Icons.Rounded.Restaurant to buildString {
            append(if (footer.recipeCount == 1) "1 recipe" else "${footer.recipeCount} recipes")
            append(" and ")
            append(if (footer.byHandCount == 1) "1 extra" else "${footer.byHandCount} extras")
        }
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(widthDp = 360, heightDp = 720)
@Composable
private fun ShoppingListsExpandedPreview() {
    fun text(value: String) = UiText.Dynamic(value)
    val recipe = RecipeThumbUi("r1", text("Beef and broccoli"), "")
    val lists = listOf(
        ShoppingListCardUi("1", text("Tonight's dinner"), "u", 11, 1, 1 / 11f, ListCardFooterUi.OneRecipe(recipe, 11, 1)),
        ShoppingListCardUi(
            "2", text("Week of Oct 26"), "u", 34, 0, 0f,
            ListCardFooterUi.SeveralRecipes(
                listOf(recipe), 4,
                listOf("Beef and broccoli", "Shakshuka", "Pad see ew", "Miso salmon").map(::text),
            ),
        ),
        ShoppingListCardUi(
            "3", text("Sunday brunch"), "u", 15, 3, 0.2f,
            ListCardFooterUi.RecipesAndExtras(listOf(recipe), 2, listOf(text("Shakshuka")), 10, 5),
        ),
        ShoppingListCardUi("4", text("Costco run"), "u", 9, 6, 6 / 9f, ListCardFooterUi.AddedByHand(listOf(text("Paper towels")), 9)),
    )
    SimmerlyTheme {
        Surface(modifier = Modifier.fillMaxHeight()) {
            ShoppingListsExpanded(
                state = ShoppingListsState(allLists = lists, lists = lists, filter = ListFilter.Mine),
                selectedListId = "3",
                onEvent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}
