package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.AppBarRow
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.VerticalScrollbar
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeTab
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.label

/** Horizontal inset shared by the action bar, the tab row and the tab bodies. */
private val EXPANDED_HORIZONTAL_PADDING = 28.dp

/** Keeps the single-column content readable on very wide panes. */
private val EXPANDED_CONTENT_MAX_WIDTH = 1040.dp

/** Six action icons (favorite, shopping list, comments, edit, settings, timeline) plus the overflow button, which maxItemCount counts. */
private const val EXPANDED_TOP_BAR_MAX_ITEMS = 7

/** With the comments pane or settings sheet open the detail pane is narrower: three icons plus the overflow button. */
private const val EXPANDED_TOP_BAR_MAX_ITEMS_COMMENTS_OPEN = 4


/** Trailing space so the last card/step clears the Start cooking FAB (same as the foldable). */
private val EXPANDED_BOTTOM_PADDING = 96.dp

/**
 * The detail pane of the list + detail layout. Mirrors the foldable design (tinted cards, two
 * column ingredients/notes, nutrition tiles, AppBarRow actions, extended FAB) minus the overview,
 * with real tabs instead of scroll-linked ones. Comments open in the list-detail scene's extra pane.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun ExpandedView(
    state: RecipeDetailsState,
    onEvent: (RecipeDetailsIntent) -> Unit,
    onToggleComments: (recipeId: String) -> Unit,
    commentsOpen: Boolean,
    /** A side panel (comments or settings) shares the window, so the pane uses the phone's single-column sections. */
    narrow: Boolean = false,
    onStartCooking: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    val recipe = state.recipe
    // Same sections as the mobile page, minus the overview: desktop has no hero or summary.
    val expandedTabs = state.mobileTabs.filter { it != RecipeTab.Overview }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var programmaticTarget by remember { mutableStateOf<Int?>(null) }

    // Section i of the page is tab i: Ingredients, Instructions, then Notes and Nutrition when
    // present, in the same order as the tabs. Scrolling drives the selection and clicking a tab
    // scrolls, as on the foldable.
    val selectedTabIndex by remember(expandedTabs) {
        derivedStateOf {
            programmaticTarget?.let { return@derivedStateOf it }
            if (expandedTabs.isEmpty()) return@derivedStateOf 0
            val index = if (!listState.canScrollForward && listState.canScrollBackward) {
                expandedTabs.lastIndex
            } else {
                listState.firstVisibleItemIndex
            }
            index.coerceIn(0, expandedTabs.lastIndex)
        }
    }

    // With the comments pane or settings sheet open the detail pane is about as narrow as a phone, so the sections
    // use the phone's single-column layout and tighter gutters.
    val contentPadding = if (narrow) 16.dp else EXPANDED_HORIZONTAL_PADDING

    val cardColors = CardDefaults.outlinedCardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow
    )
    val cardModifier = Modifier.widthIn(max = EXPANDED_CONTENT_MAX_WIDTH).fillMaxWidth()

    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Rules a line down the left edge of the detail pane, separating it from the list.
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Column(modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding()) {
                ExpandedHeader(
                    state = state,
                    tabs = expandedTabs,
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { index ->
                        coroutineScope.launch {
                            programmaticTarget = index
                            try {
                                listState.animateScrollToItem(index)
                            } finally {
                                programmaticTarget = null
                            }
                        }
                    },
                    onEvent = onEvent,
                    commentsOpen = commentsOpen,
                    narrow = narrow,
                    onShowComments = { onToggleComments(recipe.id) },
                    modifier = Modifier.fillMaxWidth()
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

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = contentPadding,
                            end = contentPadding,
                            top = 16.dp,
                            bottom = EXPANDED_BOTTOM_PADDING
                        ),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            RecipeSectionCard(
                                colors = cardColors,
                                loadingHeight = if (state.loading) 300.dp else null,
                                modifier = cardModifier
                            ) {
                                IngredientAndToolView(
                                    recipe = recipe,
                                    onRemoveServingButtonClick = { onEvent(RecipeDetailsIntent.RemoveServing) },
                                    onAddServingButtonClick = { onEvent(RecipeDetailsIntent.AddServing) },
                                    onAddToListClick = { onEvent(RecipeDetailsIntent.OpenAddToList) },
                                    columns = if (narrow) 1 else 2,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        item {
                            RecipeSectionCard(
                                colors = cardColors,
                                loadingHeight = if (state.loading) 600.dp else null,
                                modifier = cardModifier
                            ) {
                                InstructionView(
                                    instructions = recipe.instructions,
                                    ingredients = recipe.ingredients,
                                    imagesOnSide = !narrow,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        if (recipe.notes.isNotEmpty()) {
                            item {
                                RecipeSectionCard(colors = cardColors, modifier = cardModifier) {
                                    NotesView(recipe.notes, columns = if (narrow) 1 else 2, modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                        if (recipe.settings.showNutrition) {
                            item {
                                RecipeSectionCard(colors = cardColors, modifier = cardModifier) {
                                    NutritionView(
                                        recipe.nutrition,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                    VerticalScrollbar(
                        listState,
                        modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                    )
                }
            }
        }
        StartCookingFab(
            visible = !state.loading && recipe.instructions.isNotEmpty(),
            onClick = { onStartCooking(recipe.id) },
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        )
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp)
        )
    }
}

/**
 * The pane's header row: the section tabs on the left, the same [recipeActions] as the foldable
 * top bar on the right. The Comments action opens the comments extra pane.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ExpandedHeader(
    state: RecipeDetailsState,
    tabs: List<RecipeTab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onEvent: (RecipeDetailsIntent) -> Unit,
    onShowComments: () -> Unit,
    commentsOpen: Boolean,
    narrow: Boolean,
    modifier: Modifier = Modifier
) {
    val uriHandler = LocalUriHandler.current
    val actions = recipeActions(state, onEvent, { onShowComments() }, uriHandler::openUri, commentsOpen)
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(end = EXPANDED_HORIZONTAL_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // PrimaryTabRow indexes tabPositions[selectedTabIndex] unguarded, so an empty row
            // (the state while the recipe loads) crashes on measure.
            if (tabs.isNotEmpty()) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex.coerceIn(0, tabs.lastIndex),
                    edgePadding = EXPANDED_HORIZONTAL_PADDING,
                    divider = {},
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { onTabSelected(index) },
                            text = { Text(title.label) }
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            // TopAppBar/BottomAppBar tint their action icons onSurfaceVariant; this row sits on the
            // plain surface, so set the same content color to match phone and foldable.
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                AppBarRow(
                    maxItemCount = if (narrow) {
                        EXPANDED_TOP_BAR_MAX_ITEMS_COMMENTS_OPEN
                    } else {
                        EXPANDED_TOP_BAR_MAX_ITEMS
                    },
                    content = actions
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}
