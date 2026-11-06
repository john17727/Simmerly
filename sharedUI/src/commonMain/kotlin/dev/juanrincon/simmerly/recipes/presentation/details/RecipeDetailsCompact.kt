package dev.juanrincon.simmerly.recipes.presentation.details

import dev.juanrincon.simmerly.core.presentation.ErrorMessage
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Comment
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.outlined.AddShoppingCart
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.EditCalendar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Print
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Article
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.AppBarRowScope
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope
import coil3.compose.AsyncImage
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.core.presentation.shimmerColors
import dev.juanrincon.simmerly.recipes.domain.model.Note
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeTab
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.label
import dev.juanrincon.simmerly.recipes.presentation.shared.RecipeMetaRow
import dev.juanrincon.simmerly.recipes.presentation.shared.TagRow
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import kotlinx.coroutines.launch

/**
 * Shared by the phone bottom bar and the medium top bar. AppBarRow measures its own items and
 * moves the ones that don't fit into its overflow menu. Items drop from the end, so they are
 * ordered by how often they are likely to be used, most used first.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
internal fun recipeActions(
    state: RecipeDetailsState,
    onEvent: (RecipeDetailsIntent) -> Unit,
    onNavigateToComments: (recipeId: String) -> Unit,
): AppBarRowScope.() -> Unit = {
    toggleableItem(
        checked = state.recipe.favorite,
        onCheckedChange = {},
        icon = {
            Icon(
                if (state.recipe.favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = null,
                tint = if (state.recipe.favorite) MaterialTheme.colorScheme.secondary else LocalContentColor.current
            )
        },
        label = "Favorite",
        enabled = !state.loading,
    )
    clickableItem(
        onClick = { onEvent(RecipeDetailsIntent.OpenAddToList) },
        icon = { Icon(Icons.Outlined.AddShoppingCart, contentDescription = null) },
        label = "Add to shopping list",
        enabled = !state.loading,
    )
    clickableItem(
        onClick = { onNavigateToComments(state.recipe.id) },
        icon = { Icon(Icons.AutoMirrored.Outlined.Comment, contentDescription = null) },
        label = "Comments",
        enabled = !state.loading && !state.recipe.settings.disableComments,
    )
    clickableItem(
        onClick = { /* TODO: edit action */ },
        icon = { Icon(Icons.Outlined.Edit, contentDescription = null) },
        label = "Edit",
        enabled = !state.loading,
    )
    clickableItem(
        onClick = { onEvent(RecipeDetailsIntent.ShowSettings) },
        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
        label = "Settings",
        enabled = !state.loading,
    )
    clickableItem(
        onClick = { /* TODO: timeline action */ },
        icon = { Icon(Icons.Outlined.Article, contentDescription = null) },
        label = "Timeline",
        enabled = !state.loading,
    )
    // Not built yet: disabled, as in the phone menu.
    clickableItem(
        onClick = {},
        icon = { Icon(Icons.Outlined.EditCalendar, contentDescription = null) },
        label = "Add to meal plan",
        enabled = false,
    )
    clickableItem(
        onClick = {},
        icon = { Icon(Icons.Outlined.EventAvailable, contentDescription = null) },
        label = "Mark as made today",
        enabled = false,
    )
    clickableItem(
        onClick = {},
        icon = { Icon(Icons.Outlined.Share, contentDescription = null) },
        label = "Share",
        enabled = false,
    )
    clickableItem(
        onClick = {},
        icon = { Icon(Icons.Outlined.ContentCopy, contentDescription = null) },
        label = "Duplicate",
        enabled = false,
    )
    clickableItem(
        onClick = {},
        icon = { Icon(Icons.Outlined.Print, contentDescription = null) },
        label = "Print",
        enabled = false,
    )
    clickableItem(
        onClick = {},
        icon = { Icon(Icons.Outlined.Delete, contentDescription = null) },
        label = "Delete recipe",
        enabled = false,
    )
}

/** Most items the medium top bar shows before overflowing: five icons plus the overflow button (maxItemCount counts the overflow button). */
internal const val TOP_BAR_MAX_ITEMS = 6

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactRecipeDetails(
    state: RecipeDetailsState,
    onEvent: (RecipeDetailsIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (recipeId: String) -> Unit,
    onStartCooking: (recipeId: String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val medium = isMediumWidthOrWider()
    val recipeActions = recipeActions(state, onEvent, onNavigateToComments)
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            Column {
                MediumFlexibleTopAppBar(
                    title = {
                        if (state.loading) {
                            val colors = shimmerColors()
                            Box(
                                modifier = Modifier
                                    .width(160.dp)
                                    .height(20.dp)
                                    .clip(MaterialTheme.shapes.small)
                                    .shimmer(colors, MaterialTheme.shapes.small)
                            )
                        } else {
                            val isCollapsed = scrollBehavior.state.collapsedFraction > 0.5f
                            Text(
                                text = state.recipe.title.asString(),
                                maxLines = if (isCollapsed) 1 else Int.MAX_VALUE,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
                            )
                        }
                    },
                    actions = {
                        if (medium) {
                            AppBarRow(maxItemCount = TOP_BAR_MAX_ITEMS, content = recipeActions)
                        }
                    },
                    scrollBehavior = scrollBehavior
                )
                AnimatedVisibility(
                    visible = state.isRefreshing,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
            }
        },
        bottomBar = {
            if (!medium) {
                BottomAppBar(
                    actions = { AppBarRow(content = recipeActions) },
                    floatingActionButton = {
                        // No steps to cook means nothing for Cook Mode to show — keep the FAB hidden
                        // rather than opening an empty stepper.
                        AnimatedVisibility(visible = !state.loading && state.recipe.instructions.isNotEmpty()) {
                            FloatingActionButton(
                                onClick = { onStartCooking(state.recipe.id) },
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                            }
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (medium) {
                // Medium and foldable widths drop the bottom bar: the actions move to the top bar
                // and Start cooking floats on its own, as the design draws it.
                StartCookingFab(
                    visible = !state.loading && state.recipe.instructions.isNotEmpty(),
                    onClick = { onStartCooking(state.recipe.id) },
                )
            }
        }
    ) { paddingValues ->
        if (state.error != null) {
            ErrorMessage(
                message = "Couldn't load recipe. Check your connection and try again.",
                onAction = onNavigateBack,
                actionLabel = "Go back",
                modifier = modifier.padding(paddingValues),
            )
        } else {
            CompactContent(
                state = state,
                onEvent = onEvent,
                paddingValues = paddingValues,
                sharedTransitionScope = sharedTransitionScope,
                medium = medium,
                modifier = modifier.fillMaxSize()
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
private fun CompactContent(
    state: RecipeDetailsState,
    onEvent: (RecipeDetailsIntent) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
    medium: Boolean = false
) {
    val recipe = state.recipe
    val tabs = state.mobileTabs
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val tabRowHeightPx = remember(density) { with(density) { TAB_ROW_HEIGHT.roundToPx() } }

    var programmaticScrollTargetIndex by remember { mutableStateOf<Int?>(null) }

    // The medium layout folds the hero and summary into one item, so everything shifts up by one.
    val contentStartIndex = if (medium) CONTENT_START_INDEX - 1 else CONTENT_START_INDEX

    val selectedTabIndex by remember(tabs, medium) {
        derivedStateOf {
            programmaticScrollTargetIndex?.let { return@derivedStateOf it }

            if (!listState.canScrollForward) return@derivedStateOf tabs.lastIndex

            val visibleItems = listState.layoutInfo.visibleItemsInfo

            val activeItem = visibleItems
                .filter { it.index >= contentStartIndex && it.index < contentStartIndex + tabs.size - CONTENT_TAB_OFFSET }
                .lastOrNull { it.offset <= tabRowHeightPx }
                ?: return@derivedStateOf OVERVIEW_TAB_INDEX

            (activeItem.index - contentStartIndex + CONTENT_TAB_OFFSET).coerceIn(
                OVERVIEW_TAB_INDEX,
                tabs.lastIndex
            )
        }
    }

    // Section cards are tinted (Neutral50) on every layout.
    val cardColors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )

    LazyColumn(
        state = listState,
        modifier = modifier.padding(paddingValues),
        contentPadding = PaddingValues(
            top = 8.dp,
            // Medium widths float the FAB over the list (56dp + 16dp margin), so leave room to
            // scroll the last item clear of it. Phones keep the FAB inside the bottom bar.
            bottom = if (medium) 96.dp else 8.dp
        )
    ) {

        if (medium) {
            // Hero beside the description + meta: stacked, each would stretch across the window.
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    RecipeHero(
                        state = state,
                        sharedTransitionScope = sharedTransitionScope,
                        modifier = Modifier.weight(1f).height(MEDIUM_HERO_HEIGHT)
                    )
                    RecipeSummary(state, modifier = Modifier.weight(1f))
                }
            }
        } else {
            // Hero image
            item {
                RecipeHero(
                    state = state,
                    sharedTransitionScope = sharedTransitionScope,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight(0.45f)
                        .padding(horizontal = 16.dp)
                )
            }

            // Description + meta
            item {
                RecipeSummary(state, modifier = Modifier.padding(horizontal = 24.dp))
            }
        }

        // Sticky tab row
        if (tabs.isNotEmpty()) {
            stickyHeader {
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                coroutineScope.launch {
                                    programmaticScrollTargetIndex = index
                                    try {
                                        if (index == OVERVIEW_TAB_INDEX) {
                                            listState.animateScrollToItem(index = 0)
                                        } else {
                                            listState.animateScrollToItem(
                                                index = index - CONTENT_TAB_OFFSET + contentStartIndex,
                                                scrollOffset = tabRowHeightPx * -1
                                            )
                                        }
                                    } finally {
                                        programmaticScrollTargetIndex = null
                                    }
                                }
                            },
                            text = { Text(title.label) }
                        )
                    }
                }
            }
        }

        // Ingredients & Tools (always shown, index 3)
        item {
            RecipeSectionCard(
                colors = cardColors,
                loadingHeight = if (state.loading) 400.dp else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp)
            ) {
                IngredientAndToolView(
                    recipe = recipe,
                    onRemoveServingButtonClick = { onEvent(RecipeDetailsIntent.RemoveServing) },
                    onAddServingButtonClick = { onEvent(RecipeDetailsIntent.AddServing) },
                    onAddToListClick = { onEvent(RecipeDetailsIntent.OpenAddToList) },
                    columns = if (medium) MEDIUM_INGREDIENT_COLUMNS else 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Instructions (always shown, index 4)
        item {
            RecipeSectionCard(
                colors = cardColors,
                loadingHeight = if (state.loading) 600.dp else null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp)
            ) {
                InstructionView(
                    instructions = recipe.instructions,
                    ingredients = recipe.ingredients,
                    imagesOnSide = medium,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Notes (conditional)
        if (recipe.notes.isNotEmpty()) {
            item {
                RecipeSectionCard(
                    colors = cardColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    NotesView(
                        recipe.notes,
                        columns = if (medium) 2 else 1,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Nutrition (conditional)
        if (recipe.settings.showNutrition) {
            item {
                RecipeSectionCard(
                    colors = cardColors,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    NutritionView(
                        recipe.nutrition,
                        tileColumns = if (medium) 4 else 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun RecipeHero(
    state: RecipeDetailsState,
    modifier: Modifier = Modifier,
    sharedTransitionScope: SharedTransitionScope? = null,
) {
    val recipe = state.recipe
    val uriHandler = LocalUriHandler.current
    val (sharedModifier, isNavTransitionRunning) = if (sharedTransitionScope != null) {
        val animatedScope = LocalNavAnimatedContentScope.current
        val shared = with(sharedTransitionScope) {
            Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "recipe-image-${recipe.id}"),
                animatedVisibilityScope = animatedScope,
            )
        }
        shared to animatedScope.transition.isRunning
    } else {
        Modifier to false
    }
    Box(modifier = modifier) {
        AsyncImage(
            model = recipe.image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .matchParentSize()
                .then(sharedModifier)
                .ifTrue(state.loading) {
                    shimmer(
                        colors = shimmerColors(),
                        shape = MaterialTheme.shapes.medium
                    )
                }.clip(MaterialTheme.shapes.medium)
        )
        AnimatedVisibility(
            !state.loading && recipe.tags.isNotEmpty() && !isNavTransitionRunning,
            modifier = Modifier.align(Alignment.BottomStart),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            TagRow(
                tags = recipe.tags,
                modifier = Modifier
                    .padding(12.dp)
            )
        }
        AnimatedVisibility(
            !state.loading && recipe.link != null && !isNavTransitionRunning,
            modifier = Modifier.align(Alignment.BottomEnd),
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            FilledIconButton(
                onClick = { uriHandler.openUri(recipe.link!!) },
                shape = IconButtonDefaults.extraSmallSquareShape,
                modifier = Modifier.padding(12.dp)
                    .size(IconButtonDefaults.extraSmallContainerSize())
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.OpenInNew,
                    contentDescription = "Open original recipe",
                    modifier = Modifier.size(IconButtonDefaults.extraSmallIconSize)
                )
            }
        }
    }
}

@Composable
internal fun RecipeSummary(
    state: RecipeDetailsState,
    modifier: Modifier = Modifier,
) {
    val recipe = state.recipe
    val colors = shimmerColors()
    var descriptionExpanded by remember { mutableStateOf(false) }
    var descriptionOverflowing by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.height(16.dp))
        if (state.loading) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.width(64.dp).height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(colors, MaterialTheme.shapes.small)
                )
                Box(
                    modifier = Modifier.width(80.dp).height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(colors, MaterialTheme.shapes.small)
                )
                Box(
                    modifier = Modifier.width(72.dp).height(16.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(colors, MaterialTheme.shapes.small)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(colors, MaterialTheme.shapes.small)
                )
                Box(
                    modifier = Modifier.fillMaxWidth().height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(colors, MaterialTheme.shapes.small)
                )
                Box(
                    modifier = Modifier.fillMaxWidth(0.9f).height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(colors, MaterialTheme.shapes.small)
                )
                Box(
                    modifier = Modifier.fillMaxWidth(0.55f).height(14.dp)
                        .clip(MaterialTheme.shapes.small)
                        .shimmer(colors, MaterialTheme.shapes.small)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            RecipeMetaRow(
                recipe.rating,
                recipe.totalTime,
                recipe.prepTime,
                recipe.performTime
            )
            Spacer(modifier = Modifier.height(16.dp))
            recipe.description?.let {
                Column(modifier = Modifier.animateContentSize()) {
                    Text(
                        it.asString(),
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = if (descriptionExpanded) Int.MAX_VALUE else DESCRIPTION_MAX_LINES,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = { layoutResult ->
                            if (!descriptionExpanded) {
                                descriptionOverflowing = layoutResult.hasVisualOverflow
                            }
                        }
                    )
                    if (descriptionOverflowing || descriptionExpanded) {
                        TextButton(
                            onClick = { descriptionExpanded = !descriptionExpanded },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(if (descriptionExpanded) "Show less" else "Read more")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// Lazy list layout: image(0), meta(1), stickyHeader(2), content sections start at 3. In the
// medium layout the image and meta share item 0, so the sections start at 2.
private const val CONTENT_START_INDEX = 3

// Height of the hero image beside the summary; the phone's 45%-of-screen is too tall there
private val MEDIUM_HERO_HEIGHT = 300.dp

// Columns the ingredient list uses in the medium layout
private const val MEDIUM_INGREDIENT_COLUMNS = 2

// Number of tabs before the first content tab (just Overview)
private const val CONTENT_TAB_OFFSET = 1

// Index of the Overview tab
private const val OVERVIEW_TAB_INDEX = 0

// Approximate height of the sticky tab row — used to trigger tab switches and align scroll targets
private val TAB_ROW_HEIGHT = 48.dp

// Max collapsed lines for the recipe description before a "Read more" button appears
private const val DESCRIPTION_MAX_LINES = 4

private val previewRecipe = RecipeDetailUi(
    id = "1",
    title = UiText.Dynamic("Spaghetti Carbonara"),
    image = "",
    description = UiText.Dynamic(
        "A classic Roman pasta dish made with eggs, Pecorino Romano, guanciale, and black pepper. " +
                "Rich, creamy, and deeply satisfying without a drop of cream."
    ),
    rating = 4.8,
    totalTime = "30 min",
    prepTime = "10 min",
    performTime = "20 min",
    servings = 2.0,
    favorite = false,
    ingredients = listOf(
        IngredientUi(
            referenceId = "ingredient-1",
            quantity = null,
            display = "200g spaghetti",
            food = null,
            unit = null,
            note = null
        ),
        IngredientUi(
            referenceId = "ingredient-2",
            quantity = null,
            display = "100g guanciale",
            food = null,
            unit = null,
            note = null
        ),
        IngredientUi(
            referenceId = "ingredient-3",
            quantity = null,
            display = "2 large eggs",
            food = null,
            unit = null,
            note = null
        ),
        IngredientUi(
            referenceId = "ingredient-4",
            quantity = null,
            display = "50g Pecorino Romano",
            food = null,
            unit = null,
            note = null
        ),
    ),
    instructions = listOf(
        InstructionUi(
            id = "1",
            title = "Cook the pasta",
            summary = "Boil spaghetti in salted water until al dente.",
            text = "Bring a large pot of salted water to a boil. Cook spaghetti according to package instructions until al dente. Reserve 1 cup of pasta water before draining.",
            ingredientIds = listOf("ingredient-1")
        ),
        InstructionUi(
            id = "2",
            title = "Prepare the sauce",
            summary = "Whisk eggs and cheese, then combine with pasta.",
            text = "Whisk together eggs and Pecorino Romano in a bowl. Remove pasta from heat, add guanciale, then stir in egg mixture, adding pasta water gradually to achieve a creamy consistency.",
            ingredientIds = listOf("ingredient-2", "ingredient-3", "ingredient-4")
        ),
    ),
    tools = emptyList(),
    nutrition = NutritionUi(
        calories = "620 kcal",
        carbohydrateContent = "72g",
        cholesterolContent = "210mg",
        fatContent = "24g",
        fiberContent = "3g",
        proteinContent = "28g",
        saturatedFatContent = "9g",
        sodiumContent = "580mg",
        sugarContent = "2g",
        transFatContent = "0g",
        unsaturatedFatContent = "13g"
    ),
    notes = emptyList(),
    tags = emptyList(),
    link = null,
    settings = Settings(
        public = true,
        showNutrition = false,
        showAssets = false,
        landscapeView = false,
        disableComments = false,
        locked = false
    )
)

private val previewTabs = listOf(RecipeTab.Overview, RecipeTab.Ingredients, RecipeTab.Instructions)

// Fuller recipe for the medium previews, so the two-column notes and the nutrition tiles show.
private val previewMediumRecipe = previewRecipe.copy(
    notes = listOf(
        Note(id = 1, title = "A word on guanciale.", text = "Guanciale is a salty cured pork product made from pork jowls. If you can't find it, use pancetta."),
        Note(id = 2, title = "The cheese.", text = "Carbonara is a Roman pasta, so Pecorino Romano is traditional. Parmigiano Reggiano works with a little extra salt."),
        Note(id = 3, title = "Avoid scrambled eggs.", text = "Take the pan off the heat before the eggs go in so they thicken into a sauce."),
    ),
    settings = previewRecipe.settings.copy(showNutrition = true)
)

private val previewMediumTabs = RecipeTab.entries.filter {
    it in listOf(
        RecipeTab.Overview, RecipeTab.Ingredients, RecipeTab.Instructions,
        RecipeTab.Notes, RecipeTab.Nutrition
    )
}

@Preview(apiLevel = 36, showSystemUi = true, device = "spec:width=700dp,height=900dp,dpi=320")
@Composable
private fun MediumPreview() {
    SimmerlyTheme {
        CompactRecipeDetails(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewMediumRecipe,
                mobileTabs = previewMediumTabs
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = "spec:width=700dp,height=900dp,dpi=320")
@Composable
private fun MediumDarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        CompactRecipeDetails(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewMediumRecipe,
                mobileTabs = previewMediumTabs
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicPreview() {
    SimmerlyTheme(dynamicColor = true) {
        CompactRecipeDetails(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewRecipe,
                mobileTabs = previewTabs
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun LightPreview() {
    SimmerlyTheme {
        CompactRecipeDetails(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewRecipe,
                mobileTabs = previewTabs
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicDarkPreview() {
    SimmerlyTheme(darkTheme = true, dynamicColor = true) {
        CompactRecipeDetails(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewRecipe,
                mobileTabs = previewTabs
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun DarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        CompactRecipeDetails(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewRecipe,
                mobileTabs = previewTabs
            ),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun LoadingPreview() {
    SimmerlyTheme {
        CompactRecipeDetails(
            state = RecipeDetailsState(loading = true),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}


@Preview(apiLevel = 36, showSystemUi = true, device = "spec:width=1280dp,height=800dp,dpi=160")
@Composable
private fun ExpandedPreview() {
    SimmerlyTheme {
        ExpandedView(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewMediumRecipe,
                mobileTabs = previewMediumTabs
            ),
            onEvent = {},
            onStartCooking = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = "spec:width=1280dp,height=800dp,dpi=160")
@Composable
private fun ExpandedDarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        ExpandedView(
            state = RecipeDetailsState(
                loading = false,
                recipe = previewMediumRecipe,
                mobileTabs = previewMediumTabs
            ),
            onEvent = {},
            onStartCooking = {}
        )
    }
}
