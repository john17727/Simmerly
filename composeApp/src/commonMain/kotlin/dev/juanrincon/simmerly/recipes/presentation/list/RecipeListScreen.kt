package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore
import dev.juanrincon.simmerly.recipes.presentation.shared.RecipeMetaRow
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RecipeListScreen(
    onRecipeSelected: (recipeId: String) -> Unit,
    viewModel: RecipeListViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Content(
        state = state,
        onEvent = viewModel::onEvent,
        onRecipeSelected = onRecipeSelected,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    state: RecipeListStore.State,
    onEvent: (RecipeListStore.Intent) -> Unit,
    onRecipeSelected: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val lazyListState = rememberLazyListState()

    val shouldLoadMore by remember(lazyListState, state.recipes.size, state.isLoading) {
        derivedStateOf {
            if (state.isLoading) return@derivedStateOf false
            val info = lazyListState.layoutInfo
            val lastVisible =
                info.visibleItemsInfo.lastOrNull()?.index ?: return@derivedStateOf false
            val remaining = (state.recipes.size - 1) - lastVisible
            remaining <= 5 && lazyListState.isScrollInProgress
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) onEvent(RecipeListStore.Intent.OnLoadMore)
    }
    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        SelectableList(
            state.recipes,
            isLoading = state.isLoading,
            selected = state.selectedRecipeId,
            onRecipeSelected = onRecipeSelected,
            onSelected = { onEvent(RecipeListStore.Intent.OnRecipeSelected(it)) },
            state = lazyListState,
            modifier = modifier
        )
    } else {
        val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
        Scaffold(
            topBar = {
                LargeFlexibleTopAppBar(
                    title = { Text("Recipes") },
                    scrollBehavior = scrollBehavior
                )
            },
            modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
        ) { paddingValues ->
            List(
                state.recipes,
                isLoading = state.isLoading,
                onRecipeSelected = onRecipeSelected,
                lazyListState = lazyListState,
                modifier = modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
fun SelectableList(
    recipes: List<RecipeSummary>,
    isLoading: Boolean,
    selected: String,
    onRecipeSelected: (String) -> Unit,
    onSelected: (String) -> Unit,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        if (isLoading && recipes.isEmpty()) {
            items(6) {
                RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(recipes, key = { it.id }) { item ->
                val isSelected = item.id == selected
                RecipeCard(
                    item,
                    selected = isSelected,
                    onClick = {
                        onRecipeSelected(item.id)
                        onSelected(item.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem().ifTrue(isSelected) {
                            padding(vertical = 32.dp)
                        }
                )
            }
            if (isLoading) {
                item {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun List(
    recipes: List<RecipeSummary>,
    isLoading: Boolean,
    onRecipeSelected: (String) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        if (isLoading && recipes.isEmpty()) {
            items(6) {
                RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(recipes, key = { it.id }) { item ->
                RecipeCard(
                    item,
                    onClick = { onRecipeSelected(item.id) },
                    modifier = Modifier.fillMaxWidth().animateItem()
                )
            }
            if (isLoading) {
                item {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

@Composable
fun RecipeCard(
    recipe: RecipeSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(selected) {
        if (selected) {
            // Wait one frame so the expanded content is composed and measured
            withFrameNanos { }
            bringIntoViewRequester.bringIntoView()
        }
    }
    AnimatedContent(
        selected,
        transitionSpec = {
            fadeIn() togetherWith fadeOut() using SizeTransform(clip = false)
        },
        label = "RecipeCardExpand"
    ) { isSelected ->
        if (isSelected) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 32.dp)
                    .bringIntoViewRequester(bringIntoViewRequester),
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .data(recipe.image)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(200.dp, 400.dp)
                        .clip(MaterialTheme.shapes.medium)
                )
                Text(
                    recipe.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                RecipeMetaRow(
                    recipe.rating,
                    recipe.totalTime,
                    recipe.prepTime,
                    recipe.performTime,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                if (recipe.description.isNotBlank()) {
                    Text(
                        recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
            }
        } else {
            OutlinedCard(
                modifier = modifier,
                onClick = onClick,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(8.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(recipe.image)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).clip(MaterialTheme.shapes.medium)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            recipe.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        RecipeLimitedMetaRow(
                            recipe.rating,
                            recipe.totalTime
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecipeCardSkeleton(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.surfaceContainerHighest,
        MaterialTheme.colorScheme.surfaceContainer,
    )
    Card(modifier = modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .shimmer(shimmerColors, shape = MaterialTheme.shapes.medium)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(shimmerColors, shape = MaterialTheme.shapes.small)
                )
                Box(
                    modifier = Modifier
                        .width(80.dp)
                        .height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(shimmerColors, shape = MaterialTheme.shapes.small)
                )
            }
        }
    }
}

@Composable
private fun RecipeLimitedMetaRow(
    rating: Double?,
    totalTime: String?,
    modifier: Modifier = Modifier
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        rating?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Text(it.toString(), style = MaterialTheme.typography.bodySmall)
            }
        }
        totalTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
