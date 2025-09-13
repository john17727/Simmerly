package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocalDining
import androidx.compose.material.icons.rounded.LocalFireDepartment
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecipeListScreen(
    state: RecipeListStore.State,
    onEvent: (RecipeListStore.Intent) -> Unit,
    onOutput: (RecipeListStore.Output) -> Unit,
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
            selected = state.selectedRecipeId,
            onOutput = onOutput,
            onSelected = { onEvent(RecipeListStore.Intent.OnRecipeSelected(it)) },
            state = lazyListState,
            modifier = modifier
        )
    } else {
        List(
            state.recipes,
            onOutput,
            lazyListState,
            modifier = modifier
        )
    }
}

@Composable
fun SelectableList(
    recipes: List<RecipeSummary>,
    selected: String,
    onOutput: (RecipeListStore.Output) -> Unit,
    onSelected: (String) -> Unit,
    state: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = state,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        items(recipes, key = { it.id }) { item ->
            val isSelected = item.id == selected
            RecipeCard(
                item,
                selected = isSelected,
                onClick = {
                    onOutput(RecipeListStore.Output.SelectedRecipe(item.id))
                    onSelected(item.id)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem().ifTrue(isSelected) {
                        padding(vertical = 32.dp)
                    }
            )
        }
    }
}

@Composable
fun List(
    recipes: List<RecipeSummary>,
    onOutput: (RecipeListStore.Output) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        items(recipes, key = { it.id }) { item ->
            RecipeCard(
                item,
                onClick = { onOutput(RecipeListStore.Output.SelectedRecipe(item.id)) },
                modifier = Modifier.fillMaxWidth().animateItem()
            )
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
    Card(
        modifier = modifier
            .bringIntoViewRequester(bringIntoViewRequester)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
            .semantics {
                stateDescription = if (selected) "Expanded" else "Collapsed"
                role = Role.Button
            },
        onClick = onClick,
        colors = CardDefaults.cardColors().copy(containerColor = MaterialTheme.colorScheme.surface)
    ) {
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
                    modifier = Modifier.padding(bottom = 8.dp)
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
                        style = MaterialTheme.typography.titleMedium,
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
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            } else {
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
private fun RecipeMetaRow(
    rating: Double?,
    totalTime: String?,
    prepTime: String?,
    cookTime: String?,
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
        prepTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.LocalDining,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
        cookTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Rounded.LocalFireDepartment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(it, style = MaterialTheme.typography.bodySmall)
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
