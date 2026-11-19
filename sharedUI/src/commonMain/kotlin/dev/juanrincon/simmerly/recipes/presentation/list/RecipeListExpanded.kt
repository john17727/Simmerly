package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import kotlinx.coroutines.flow.first
import androidx.paging.compose.LazyPagingItems
import dev.juanrincon.simmerly.core.presentation.VerticalScrollbar
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

@Composable
internal fun SelectableList(
    lazyPagingItems: LazyPagingItems<RecipeSummary>,
    onFavoriteToggled: (recipeId: String, favorite: Boolean) -> Unit,
    onRatingChanged: (recipeId: String, rating: Double?) -> Unit,
    selected: String,
    onRecipeSelected: (String) -> Unit,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    // Scroll a newly selected card to the top of the list. Skipped for a selection that was already
    // there when this composed (restored after a tab switch), so the list keeps its position.
    var lastScrolledId by rememberSaveable { mutableStateOf(selected) }
    LaunchedEffect(selected) {
        if (selected.isEmpty()) {
            lastScrolledId = ""
            return@LaunchedEffect
        }
        if (selected == lastScrolledId) return@LaunchedEffect
        val index = (0 until lazyPagingItems.itemCount)
            .firstOrNull { lazyPagingItems.peek(it)?.id == selected }
            ?: return@LaunchedEffect
        lastScrolledId = selected
        state.animateScrollToItem(index)
    }

    // The ViewModel (and so the LazyPagingItems) is rebuilt when returning from another tab, and a
    // fresh LazyPagingItems is empty on its first frame, which clamps the restored list state to
    // the top. Remember the position ourselves and re-apply it once the items are back.
    var savedIndex by rememberSaveable { mutableIntStateOf(0) }
    var savedOffset by rememberSaveable { mutableIntStateOf(0) }
    var positionRestored by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (savedIndex > 0) {
            snapshotFlow { lazyPagingItems.itemCount }.first { it > 0 }
            state.scrollToItem(
                savedIndex.coerceAtMost(lazyPagingItems.itemCount - 1),
                savedOffset
            )
        }
        positionRestored = true
    }
    LaunchedEffect(state) {
        snapshotFlow { state.firstVisibleItemIndex to state.firstVisibleItemScrollOffset }
            .collect { (index, offset) ->
                if (positionRestored) {
                    savedIndex = index
                    savedOffset = offset
                }
            }
    }

    Box(modifier = modifier) {
        LazyColumn(
            state = state,
            contentPadding = PaddingValues(
                start = 16.dp,
                end = 16.dp,
                top = 16.dp + WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
                bottom = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize().clip(shape = MaterialTheme.shapes.medium)
        ) {
            // Skeletons are for the first load only; a refresh over existing items keeps them.
            if (lazyPagingItems.loadState.refresh is LoadState.Loading && lazyPagingItems.itemCount == 0) {
                items(6) {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            } else {
                items(
                    count = lazyPagingItems.itemCount,
                    key = { index -> lazyPagingItems.peek(index)?.id ?: index }
                ) { index ->
                    val item = lazyPagingItems[index] ?: return@items
                    val isSelected = item.id == selected
                    RecipeCard(
                        item,
                        onFavoriteClick = { onFavoriteToggled(item.id, it) },
                        onRatingChange = { onRatingChanged(item.id, it) },
                        selected = isSelected,
                        onClick = {
                            onRecipeSelected(item.id)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem().ifTrue(isSelected) {
                                padding(vertical = 32.dp)
                            }
                    )
                }
                if (lazyPagingItems.loadState.append is LoadState.Loading) {
                    item {
                        RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        }
        VerticalScrollbar(
            state,
            modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
        )
    }
}
