package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumFlexibleTopAppBar
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore
import dev.juanrincon.simmerly.recipes.presentation.shared.RecipeMetaRow
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactRecipeDetails(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            MediumFlexibleTopAppBar(
                title = {
                    if (state.loading) {
                        val shimmerColors = listOf(
                            MaterialTheme.colorScheme.surfaceContainer,
                            MaterialTheme.colorScheme.surfaceContainerHighest,
                            MaterialTheme.colorScheme.surfaceContainer,
                        )
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(20.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
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
                scrollBehavior = scrollBehavior
            )
        },
        bottomBar = {
            BottomAppBar(
                actions = {
                    IconToggleButton(
                        checked = state.recipe.favorite,
                        onCheckedChange = {},
                        enabled = !state.loading
                    ) {
                        Icon(
                            Icons.Default.Favorite,
                            contentDescription = "Favorite",
                        )
                    }
                    IconButton(
                        onClick = { /* TODO: timeline action */ },
                        enabled = !state.loading
                    ) {
                        Icon(Icons.Default.ViewTimeline, contentDescription = "Timeline")
                    }
                    IconButton(onClick = { /* TODO: edit action */ }, enabled = !state.loading) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = { onEvent(RecipeDetailsStore.Intent.ShowSettings) },
                        enabled = !state.loading
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(
                        onClick = { onNavigateToComments(state.recipe.id) },
                        enabled = !state.loading && !state.recipe.settings.disableComments
                    ) {
                        Icon(
                            Icons.AutoMirrored.Default.Comment,
                            contentDescription = "Comment"
                        )
                    }
                    IconButton(onClick = { /* TODO: more action */ }, enabled = !state.loading) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More")
                    }
                },
                floatingActionButton = {
                    AnimatedVisibility(visible = !state.loading) {
                        FloatingActionButton(
                            onClick = { /* TODO: play action */ },
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        CompactContent(
            state = state,
            onEvent = onEvent,
            paddingValues = paddingValues,
            modifier = modifier.fillMaxSize()
        )
    }
}

@Composable
private fun CompactContent(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    paddingValues: PaddingValues,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val tabs = state.mobileTabs
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val tabRowHeightPx = remember(density) { with(density) { TAB_ROW_HEIGHT.roundToPx() } }

    var programmaticScrollTargetIndex by remember { mutableStateOf<Int?>(null) }

    val selectedTabIndex by remember(tabs) {
        derivedStateOf {
            programmaticScrollTargetIndex?.let { return@derivedStateOf it }

            if (!listState.canScrollForward) return@derivedStateOf tabs.lastIndex

            val visibleItems = listState.layoutInfo.visibleItemsInfo

            val activeItem = visibleItems
                .filter { it.index >= CONTENT_START_INDEX && it.index < CONTENT_START_INDEX + tabs.size - CONTENT_TAB_OFFSET }
                .lastOrNull { it.offset <= tabRowHeightPx }
                ?: return@derivedStateOf OVERVIEW_TAB_INDEX

            (activeItem.index - CONTENT_START_INDEX + CONTENT_TAB_OFFSET).coerceIn(
                OVERVIEW_TAB_INDEX,
                tabs.lastIndex
            )
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.padding(paddingValues),
        contentPadding = PaddingValues(
            top = 8.dp,
            bottom = 8.dp
        )
    ) {

        // Hero image
        item {
            AsyncImage(
                model = recipe.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillParentMaxHeight(0.33f).padding(horizontal = 16.dp)
                    .ifTrue(state.loading) {
                        height(100.dp).shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }.clip(MaterialTheme.shapes.medium)
            )
        }

        // Description + meta
        item {
            val shimmerColors = listOf(
                MaterialTheme.colorScheme.surfaceContainer,
                MaterialTheme.colorScheme.surfaceContainerHighest,
                MaterialTheme.colorScheme.surfaceContainer,
            )
            var descriptionExpanded by remember { mutableStateOf(false) }
            var descriptionOverflowing by remember { mutableStateOf(false) }
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                if (state.loading) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(
                            modifier = Modifier.width(64.dp).height(16.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
                        )
                        Box(
                            modifier = Modifier.width(80.dp).height(16.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
                        )
                        Box(
                            modifier = Modifier.width(72.dp).height(16.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(
                            modifier = Modifier.fillMaxWidth().height(14.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth().height(14.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(0.9f).height(14.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
                        )
                        Box(
                            modifier = Modifier.fillMaxWidth(0.55f).height(14.dp)
                                .clip(MaterialTheme.shapes.small)
                                .shimmer(shimmerColors, MaterialTheme.shapes.small)
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
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
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
                                                index = index - CONTENT_TAB_OFFSET + CONTENT_START_INDEX,
                                                scrollOffset = tabRowHeightPx * -1
                                            )
                                        }
                                    } finally {
                                        programmaticScrollTargetIndex = null
                                    }
                                }
                            },
                            text = { Text(title) }
                        )
                    }
                }
            }
        }

        // Ingredients & Tools (always shown, index 3)
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp)
                    .ifTrue(state.loading) {
                        height(400.dp).shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
            ) {
                IngredientAndToolView(
                    recipe = recipe,
                    onRemoveServingButtonClick = { onEvent(RecipeDetailsStore.Intent.RemoveServing) },
                    onAddServingButtonClick = { onEvent(RecipeDetailsStore.Intent.AddServing) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Instructions (always shown, index 4)
        item {
            OutlinedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(horizontal = 16.dp)
                    .ifTrue(state.loading) {
                        height(600.dp).shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
            ) {
                InstructionView(
                    instructions = recipe.instructions,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Notes (conditional)
        if (recipe.notes.isNotEmpty()) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    NotesView(
                        recipe.notes,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Nutrition (conditional)
        if (recipe.settings.showNutrition) {
            item {
                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .padding(horizontal = 16.dp)
                ) {
                    NutritionView(
                        recipe.nutrition,
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(unbounded = true)
                            .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                    )
                }
            }
        }
    }
}

// Lazy list layout: image(0), meta(1), stickyHeader(2), content sections start at 3
private const val CONTENT_START_INDEX = 3

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
    title = UiText.DynamicText("Spaghetti Carbonara"),
    image = "",
    description = UiText.DynamicText(
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
            quantity = null,
            display = "200g spaghetti",
            food = null,
            unit = null,
            note = null
        ),
        IngredientUi(
            quantity = null,
            display = "100g guanciale",
            food = null,
            unit = null,
            note = null
        ),
        IngredientUi(
            quantity = null,
            display = "2 large eggs",
            food = null,
            unit = null,
            note = null
        ),
        IngredientUi(
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
            associatedIngredients = emptyList()
        ),
        InstructionUi(
            id = "2",
            title = "Prepare the sauce",
            summary = "Whisk eggs and cheese, then combine with pasta.",
            text = "Whisk together eggs and Pecorino Romano in a bowl. Remove pasta from heat, add guanciale, then stir in egg mixture, adding pasta water gradually to achieve a creamy consistency.",
            associatedIngredients = emptyList()
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
    settings = Settings(
        public = true,
        showNutrition = false,
        showAssets = false,
        landscapeView = false,
        disableComments = false,
        locked = false
    )
)

private val previewTabs = listOf("Overview", "Ingredients", "Instructions")

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicPreview() {
    SimmerlyTheme(dynamicColor = true) {
        CompactRecipeDetails(
            state = RecipeDetailsStore.State(
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
            state = RecipeDetailsStore.State(
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
            state = RecipeDetailsStore.State(
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
            state = RecipeDetailsStore.State(
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
            state = RecipeDetailsStore.State(loading = true),
            onEvent = {},
            onNavigateBack = {},
            onNavigateToComments = {}
        )
    }
}
