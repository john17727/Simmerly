package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.skydoves.compose.stability.runtime.TraceRecomposition
//import com.skydoves.compose.stability.runtime.TraceRecomposition
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.domain.model.Note
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsScreen
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore
import dev.juanrincon.simmerly.recipes.presentation.shared.RecipeMetaRow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RecipeDetailsScreen(
    recipeId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToComments: (recipeId: String) -> Unit,
    viewModel: RecipeDetailsViewModel = koinViewModel { parametersOf(recipeId) },
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Content(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToComments = onNavigateToComments,
        modifier = modifier
    )
}

@TraceRecomposition
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun Content(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showSettings) {
        ModalBottomSheet(
            dragHandle = null,
            onDismissRequest = {
                onEvent(RecipeDetailsStore.Intent.DismissSettings)
            },
        ) {
            SettingsView(
                settings = state.recipe.settings,
                onSettingChanged = { onEvent(RecipeDetailsStore.Intent.UpdateSettings(it)) },
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isExpanded =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    AnimatedContent(state.mode) { mode ->
        when (mode) {
            RecipeDetailsStore.RecipeMode.READ_ONLY -> {
                AnimatedContent(isExpanded) { expanded ->
                    if (expanded) {
                        ExpandedView(
                            state = state,
                            onEvent = onEvent,
                            modifier = modifier
                        )
                    } else {
                        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
                        Scaffold(
                            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
                            topBar = {
                                MediumTopAppBar(
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
                                                    .shimmer(
                                                        shimmerColors,
                                                        MaterialTheme.shapes.small
                                                    )
                                            )
                                        } else {
                                            val isCollapsed =
                                                scrollBehavior.state.collapsedFraction > 0.5f
                                            Text(
                                                text = state.recipe.title,
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
                                        if (!state.recipe.settings.disableComments) {
                                            IconButton(onClick = { onNavigateToComments(state.recipe.id) }) {
                                                Icon(
                                                    Icons.AutoMirrored.Default.Comment,
                                                    contentDescription = "Comment"
                                                )
                                            }
                                        }
                                    },
                                    colors = TopAppBarDefaults.topAppBarColors()
                                        .copy(containerColor = MaterialTheme.colorScheme.background),
                                    scrollBehavior = scrollBehavior
                                )
                            },
                        ) { paddingValues ->
                            Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
                                CompactView(
                                    state = state,
                                    onEvent = onEvent,
                                    modifier = Modifier.fillMaxSize()
                                )
                                HorizontalFloatingToolbar(
                                    expanded = true,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .padding(bottom = 24.dp)
                                ) {
                                    IconButton(onClick = { /* TODO: favourite action */ }) {
                                        Icon(
                                            Icons.Default.Favorite,
                                            contentDescription = "Favourite"
                                        )
                                    }
                                    IconButton(onClick = { onEvent(RecipeDetailsStore.Intent.ShowSettings) }) {
                                        Icon(
                                            Icons.Default.Settings,
                                            contentDescription = "Settings"
                                        )
                                    }
                                    IconButton(onClick = { /* TODO: edit action */ }) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Edit"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            RecipeDetailsStore.RecipeMode.EDIT -> TODO()
        }
    }
}

@Composable
private fun CompactView(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val tabs = state.mobileTabs
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current
    val tabRowHeightPx = remember(density) { with(density) { TAB_ROW_HEIGHT.roundToPx() } }

    // When the user taps a tab we lock the indicator to that index for the
    // duration of the programmatic animated scroll, preventing intermediate
    // positions from momentarily lighting up the wrong tab.
    var programmaticScrollTargetIndex by remember { mutableStateOf<Int?>(null) }

    // List layout: image(0), meta(1), stickyHeader(2), content sections(3+), trailing Spacer
    val selectedTabIndex by remember(tabs) {
        derivedStateOf {
            // While we are animating to a tab, keep the indicator pinned there.
            programmaticScrollTargetIndex?.let { return@derivedStateOf it }

            // At the very bottom of the list, always select the last tab
            if (!listState.canScrollForward) return@derivedStateOf tabs.lastIndex

            val visibleItems = listState.layoutInfo.visibleItemsInfo

            // Find the last content card whose top edge has reached the sticky header bottom.
            // Overview (tab 0) is selected when no content card has crossed that threshold yet.
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
        modifier = modifier,
        contentPadding = PaddingValues(top = 8.dp, bottom = 96.dp)
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
            // State declared outside any conditional to satisfy Compose's rules-of-hooks
            var descriptionExpanded by remember { mutableStateOf(false) }
            var descriptionOverflowing by remember { mutableStateOf(false) }
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                if (state.loading) {
                    // Meta row skeleton — three shimmer pills mimicking icon+text chips
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
                    // Description skeleton — four lines at decreasing widths
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
                    if (recipe.description.isNotBlank()) {
                        Column(modifier = Modifier.animateContentSize()) {
                            Text(
                                recipe.description,
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
            Card(
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
            Card(
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
                Card(
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
                Card(
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

@Composable
private fun ExpandedView(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val expandedTabs = state.desktopTabs

    var selectedExpandedTabIndex by remember { mutableIntStateOf(0) }

    // Reset to first tab if the currently selected tab disappears (e.g., nutrition disabled)
    LaunchedEffect(expandedTabs) {
        if (selectedExpandedTabIndex >= expandedTabs.size) {
            selectedExpandedTabIndex = 0
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(top = 16.dp, end = 16.dp)
    ) {
        if (expandedTabs.count() != 1) {
            PrimaryTabRow(
                selectedTabIndex = selectedExpandedTabIndex,
            ) {
                expandedTabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedExpandedTabIndex == index,
                        onClick = { selectedExpandedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
        }

        AnimatedContent(
            targetState = selectedExpandedTabIndex,
            modifier = Modifier.weight(1f)
        ) { tabIndex ->
            when (expandedTabs.getOrNull(tabIndex)) {
                EXPANDED_TAB_RECIPE -> {
                    // Two-column layout: ingredients (left) + instructions (right)
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(EXPANDED_CARD_PADDING)
                    ) {
                        Column(
                            modifier = Modifier
                                .widthIn(200.dp, 300.dp)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
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
                                IngredientAndToolView(
                                    recipe = recipe,
                                    onRemoveServingButtonClick = { onEvent(RecipeDetailsStore.Intent.RemoveServing) },
                                    onAddServingButtonClick = { onEvent(RecipeDetailsStore.Intent.AddServing) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (recipe.settings.showNutrition) {
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    NutritionView(
                                        recipe.nutrition,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(unbounded = true)
                                            .padding(
                                                top = 32.dp,
                                                bottom = 16.dp,
                                                start = 16.dp,
                                                end = 16.dp
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .ifTrue(state.loading) {
                                        height(800.dp).shimmer(
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
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                }

                EXPANDED_TAB_NOTES -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        NotesView(
                            recipe.notes,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                EXPANDED_TAB_COMMENTS -> {
                    RecipeCommentsScreen(
                        recipeId = recipe.id,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
fun NotesView(notes: List<Note>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("Notes", style = MaterialTheme.typography.headlineSmall)
        }
        notes.forEach { note ->
            NoteEntry(note, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun NoteEntry(note: Note, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (note.title.isNotEmpty()) {
            Text(
                note.title, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(note.text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun NutritionView(nutrition: NutritionUi, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text("Nutrition", style = MaterialTheme.typography.headlineSmall)
        nutrition.calories?.let {
            NutritionEntry("Calories", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.carbohydrateContent?.let {
            NutritionEntry("Carbohydrates", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.cholesterolContent?.let {
            NutritionEntry("Cholesterol", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.fatContent?.let {
            NutritionEntry("Fat", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.fiberContent?.let {
            NutritionEntry("Fiber", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.proteinContent?.let {
            NutritionEntry("Protein", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.saturatedFatContent?.let {
            NutritionEntry("Saturated Fat", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.sodiumContent?.let {
            NutritionEntry("Sodium", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.sugarContent?.let {
            NutritionEntry("Sugar", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.transFatContent?.let {
            NutritionEntry("Trans Fat", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.unsaturatedFatContent?.let {
            NutritionEntry("Unsaturated Fat", it, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NutritionEntry(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title)
        Text(value, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun IngredientAndToolView(
    recipe: RecipeDetailUi,
    onAddServingButtonClick: () -> Unit,
    onRemoveServingButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("Ingredients", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(recipe.formattedServings, color = MaterialTheme.colorScheme.primary)
                if (recipe.isParsed) {
                    Row {
                        IconButton(
                            onClick = onRemoveServingButtonClick,
                            enabled = recipe.servings > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        IconButton(onClick = onAddServingButtonClick) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        }
        recipe.ingredients.forEach { ingredient ->
            IngredientEntry(ingredient, modifier = Modifier.fillMaxWidth())
        }
        if (recipe.tools.isNotEmpty()) {
            Text(
                "Tools",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            recipe.tools.forEach { tool ->
                Text(tool.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InstructionView(
    instructions: List<InstructionUi>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("Instructions", style = MaterialTheme.typography.headlineSmall)
        }
        instructions.forEach { instruction ->
            InstructionEntry(instruction, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun IngredientEntry(ingredient: IngredientUi, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            ingredient.formattedQuantity?.let {
                Text(it, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(ingredient.formattedDisplay)
        }
        ingredient.note?.let { note ->
            Text(
                note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun InstructionEntry(instruction: InstructionUi, modifier: Modifier = Modifier) {
    val richTextState = rememberRichTextState()
    LaunchedEffect(instruction.text) {
        richTextState.setMarkdown(instruction.text)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        Text(
            instruction.summary,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Row {
            instruction.associatedIngredients.forEach {
                Text(it.formattedDisplay, style = MaterialTheme.typography.bodySmall)
            }
        }
        RichText(
            state = richTextState,
            imageLoader = Coil3ImageLoader,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsView(
    settings: Settings,
    onSettingChanged: (Settings) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Recipe Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(
                checked = settings.public,
                onCheckedChange = { onSettingChanged(settings.copy(public = it)) }
            )
            Text("Public Recipe")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(
                checked = settings.showNutrition,
                onCheckedChange = { onSettingChanged(settings.copy(showNutrition = it)) }
            )
            Text("Show Nutrition Values")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(
                checked = settings.disableComments,
                onCheckedChange = { onSettingChanged(settings.copy(disableComments = it)) }
            )
            Text("Disable Comments")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(
                checked = settings.locked,
                onCheckedChange = { onSettingChanged(settings.copy(locked = it)) }
            )
            Text("Locked")
        }
    }
}

val EXPANDED_CARD_PADDING = 16.dp
val SETTINGS_OPTION_SPACING = 16.dp

private const val EXPANDED_TAB_RECIPE = "Recipe"
private const val EXPANDED_TAB_NOTES = "Notes"
private const val EXPANDED_TAB_COMMENTS = "Comments"

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


@Preview
@Composable
fun ExpandedViewPreview() {
//    ExpandedView(state = RecipeDetail(), modifier = Modifier.fillMaxSize())
}
