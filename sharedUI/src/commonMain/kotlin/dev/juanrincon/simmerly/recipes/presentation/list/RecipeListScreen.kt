package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListIntent
import dev.juanrincon.simmerly.recipes.presentation.list.orbit.RecipeListState
import dev.juanrincon.simmerly.recipes.presentation.shared.CategoriesAndTags
import dev.juanrincon.simmerly.recipes.presentation.shared.RatingInput
import dev.juanrincon.simmerly.recipes.presentation.shared.RatingStars
import dev.juanrincon.simmerly.recipes.presentation.shared.RecipeTimes
import dev.juanrincon.simmerly.recipes.presentation.shared.TagRow
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun RecipeListScreen(
    onRecipeSelected: (recipeId: String) -> Unit,
    onSearchClicked: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    viewModel: RecipeListViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    val favoriteIds by viewModel.favoriteIds.collectAsState()
    val ratingOverrides by viewModel.ratingOverrides.collectAsState()
    val lazyPagingItems = viewModel.recipes.collectAsLazyPagingItems()
    Content(
        state = state,
        lazyPagingItems = lazyPagingItems,
        favoriteIds = favoriteIds,
        ratingOverrides = ratingOverrides,
        onEvent = viewModel::onEvent,
        onRecipeSelected = onRecipeSelected,
        onSearchClicked = onSearchClicked,
        sharedTransitionScope = sharedTransitionScope,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: RecipeListState,
    lazyPagingItems: LazyPagingItems<RecipeSummary>,
    favoriteIds: Set<String>,
    ratingOverrides: Map<String, Double?>,
    onEvent: (RecipeListIntent) -> Unit,
    onRecipeSelected: (recipeId: String) -> Unit,
    onSearchClicked: () -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val lazyGridState = rememberLazyGridState()

    if (isTwoPaneWidth()) {
        SelectableList(
            lazyPagingItems = lazyPagingItems,
            favoriteIds = favoriteIds,
            onFavoriteToggled = { id, favorite -> onEvent(RecipeListIntent.OnFavoriteToggled(id, favorite)) },
            ratingOverrides = ratingOverrides,
            onRatingChanged = { id, rating -> onEvent(RecipeListIntent.OnRatingChanged(id, rating)) },
            selected = state.selectedRecipeId,
            onRecipeSelected = onRecipeSelected,
            onSelected = { onEvent(RecipeListIntent.OnRecipeSelected(it)) },
            state = lazyListState,
            modifier = modifier
        )
    } else {
        CompactRecipeList(
            lazyPagingItems = lazyPagingItems,
            favoriteIds = favoriteIds,
            onFavoriteToggled = { id, favorite -> onEvent(RecipeListIntent.OnFavoriteToggled(id, favorite)) },
            onSearchClicked = onSearchClicked,
            onRecipeSelected = onRecipeSelected,
            sharedTransitionScope = sharedTransitionScope,
            lazyGridState = lazyGridState,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RecipeCard(
    recipe: RecipeSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean = false,
    sharedTransitionScope: SharedTransitionScope? = null,
    /** Called with the new favorite state when the heart is tapped. `null` makes it display-only. */
    onFavoriteClick: ((Boolean) -> Unit)? = null,
    /** Called with a whole-star rating (or `null` to clear) from the expanded card's stars. The
     * expanded card only exists in the desktop list pane. `null` makes the stars display-only. */
    onRatingChange: ((Double?) -> Unit)? = null,
) {
    val bringIntoViewRequester = remember { BringIntoViewRequester() }
    LaunchedEffect(selected) {
        if (selected) {
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
                if (onRatingChange != null) {
                    RatingInput(
                        recipe.rating,
                        onRatingChange = onRatingChange,
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                } else {
                    RatingStars(recipe.rating, modifier = Modifier.padding(horizontal = 8.dp))
                }
                if (recipe.description.isNotBlank()) {
                    Text(
                        recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                }
                RecipeTimes(
                    recipe.totalTime,
                    recipe.prepTime,
                    recipe.performTime,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
                )
                CategoriesAndTags(recipe.categories, recipe.tags)
            }
        } else {
            val interactionSource = remember { MutableInteractionSource() }
            val hovered by interactionSource.collectIsHoveredAsState()
            Box(modifier = modifier) {
            OutlinedCard(
                modifier = Modifier.fillMaxWidth().hoverable(interactionSource),
                onClick = onClick,
                interactionSource = interactionSource,
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.outlinedCardColors(
                    containerColor = if (hovered) MaterialTheme.colorScheme.surfaceContainer
                    else MaterialTheme.colorScheme.surfaceContainerLow
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(10.dp)
                ) {
                    val animatedScope = LocalNavAnimatedContentScope.current
                    val sharedModifier = if (sharedTransitionScope != null) {
                        with(sharedTransitionScope) {
                            Modifier.sharedElement(
                                sharedContentState = rememberSharedContentState(key = "recipe-image-${recipe.id}"),
                                animatedVisibilityScope = animatedScope,
                            )
                        }
                    } else Modifier
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(recipe.image)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(100.dp).then(sharedModifier)
                            .clip(MaterialTheme.shapes.small)
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        // Leaves room for the favorite indicator in the top-right corner.
                        modifier = Modifier.fillMaxWidth().padding(end = 32.dp)
                    ) {
                        Text(
                            recipe.name,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        RecipeLimitedMetaRow(
                            recipe.rating,
                            recipe.totalTime,
                        )
                        TagRow(recipe.tags)
                    }
                }
            }
            FavoriteHeart(
                favorite = recipe.isFavorite,
                onClick = onFavoriteClick?.let { click -> { click(!recipe.isFavorite) } },
                modifier = Modifier.align(Alignment.TopEnd).padding(top = 4.dp, end = 4.dp)
            )
            }
        }
    }
}

/** The heart in a card's top-right corner: outlined, or filled coral for a favorite. */
@Composable
private fun FavoriteHeart(
    favorite: Boolean,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val icon: @Composable () -> Unit = {
        Icon(
            if (favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (onClick != null) {
                if (favorite) "Remove from favorites" else "Add to favorites"
            } else if (favorite) "Favorite" else null,
            tint = if (favorite) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(22.dp)
        )
    }
    if (onClick != null) {
        IconButton(onClick = onClick, modifier = modifier.size(40.dp), content = icon)
    } else {
        Box(modifier = modifier.size(40.dp), contentAlignment = Alignment.Center) { icon() }
    }
}

@Composable
fun RecipeCardSkeleton(modifier: Modifier = Modifier) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceContainer,
        MaterialTheme.colorScheme.surfaceContainerHighest,
        MaterialTheme.colorScheme.surfaceContainer,
    )
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
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
        RatingStars(rating)
        totalTime?.let {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
