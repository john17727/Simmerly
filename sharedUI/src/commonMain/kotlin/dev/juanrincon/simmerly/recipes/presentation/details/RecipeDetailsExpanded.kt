package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ViewTimeline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.VerticalScrollbar
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsScreen
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeTab
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.label

/** Gap between the ingredients/nutrition column and the instructions column. */
private val EXPANDED_COLUMN_GAP = 32.dp

/** Horizontal inset shared by the action bar, the tab row and the two-column body. */
private val EXPANDED_HORIZONTAL_PADDING = 28.dp

/**
 * Width of the ingredients/nutrition column. Fixed, as the design draws it, but wider than its
 * 320px so the roomier quantity column doesn't eat into the ingredient names.
 */
private val EXPANDED_SIDE_COLUMN_WIDTH = 360.dp

/** Trailing breathing room so the last card/step clears the bottom of the scrolling pane. */
private val EXPANDED_BOTTOM_PADDING = 48.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpandedView(
    state: RecipeDetailsState,
    onEvent: (RecipeDetailsIntent) -> Unit,
    onStartCooking: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val expandedTabs = state.desktopTabs

    var selectedExpandedTabIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(expandedTabs) {
        if (selectedExpandedTabIndex >= expandedTabs.size) {
            selectedExpandedTabIndex = 0
        }
    }

    Row(modifier = modifier.fillMaxSize()) {
        // The design rules a line down the left edge of the detail pane, separating it from the
        // recipe list. It lives here rather than on the list because the list pane is also the
        // whole screen on compact widths, where there is nothing to separate it from.
        VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
            ExpandedHeader(
                state = state,
                tabs = expandedTabs,
                selectedTabIndex = selectedExpandedTabIndex,
                onTabSelected = { selectedExpandedTabIndex = it },
                onEvent = onEvent,
                onStartCooking = onStartCooking,
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

            AnimatedContent(
                targetState = selectedExpandedTabIndex,
                modifier = Modifier.weight(1f)
            ) { tabIndex ->
                when (expandedTabs.getOrNull(tabIndex)) {
                    RecipeTab.Recipe -> {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    start = EXPANDED_HORIZONTAL_PADDING,
                                    end = EXPANDED_HORIZONTAL_PADDING,
                                ),
                            horizontalArrangement = Arrangement.spacedBy(EXPANDED_COLUMN_GAP)
                        ) {
                            val sideScrollState = rememberScrollState()
                            val instructionScrollState = rememberScrollState()
                            Box(modifier = Modifier.width(EXPANDED_SIDE_COLUMN_WIDTH).fillMaxHeight()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(sideScrollState),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    OutlinedCard(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .ifTrue(state.loading) {
                                                height(600.dp).shimmer(
                                                    colors = shimmerColors(),
                                                    shape = MaterialTheme.shapes.medium
                                                )
                                            }
                                            .padding(top = 24.dp)
                                    ) {
                                        IngredientAndToolView(
                                            recipe = recipe,
                                            onRemoveServingButtonClick = { onEvent(RecipeDetailsIntent.RemoveServing) },
                                            onAddServingButtonClick = { onEvent(RecipeDetailsIntent.AddServing) },
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                    if (recipe.settings.showNutrition) {
                                        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                                            NutritionView(
                                                recipe.nutrition,
                                                modifier = Modifier.fillMaxWidth()
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(EXPANDED_BOTTOM_PADDING))
                                }
                                VerticalScrollbar(
                                    sideScrollState,
                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                                )
                            }
                            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .verticalScroll(instructionScrollState)
                                ) {
                                    // No card here: the design sets the instructions directly on the
                                    // surface, so only the ingredients side reads as a panel.
                                    InstructionView(
                                        instructions = recipe.instructions,
                                        ingredients = recipe.ingredients,
                                        contentPadding = PaddingValues(0.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .ifTrue(state.loading) {
                                                height(800.dp).shimmer(
                                                    colors = shimmerColors(),
                                                    shape = MaterialTheme.shapes.medium
                                                )
                                            }
                                            .padding(top = 24.dp)
                                    )
                                    Spacer(modifier = Modifier.height(EXPANDED_BOTTOM_PADDING))
                                }
                                VerticalScrollbar(
                                    instructionScrollState,
                                    modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                                )
                            }
                        }
                    }

                    RecipeTab.Notes -> {
                        val notesScrollState = rememberScrollState()
                        Box(modifier = Modifier.fillMaxSize()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = EXPANDED_HORIZONTAL_PADDING,
                                        end = EXPANDED_HORIZONTAL_PADDING,
                                        top = 24.dp
                                    )
                                    .verticalScroll(notesScrollState)
                            ) {
                                NotesView(
                                    recipe.notes,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(EXPANDED_BOTTOM_PADDING))
                            }
                            VerticalScrollbar(
                                notesScrollState,
                                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
                            )
                        }
                    }

                    RecipeTab.Comments -> {
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
}

/**
 * The pane's single header row: the section tabs on the left, then the desktop mirror of the
 * compact screen's [androidx.compose.material3.BottomAppBar] actions on the right. The Comment
 * button is deliberately absent — on desktop the comments are one of those tabs (see
 * [RecipeDetailsState.desktopTabs]), so there is nowhere to navigate to.
 *
 * The tab row's own divider is suppressed and one is drawn under the whole header instead;
 * otherwise the rule would stop where the tabs stop rather than running the width of the pane.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandedHeader(
    state: RecipeDetailsState,
    tabs: List<RecipeTab>,
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit,
    onEvent: (RecipeDetailsIntent) -> Unit,
    onStartCooking: (recipeId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .padding(end = EXPANDED_HORIZONTAL_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Only render the row when there is a choice to make. Guarding on `!= 1` would still
            // draw it for an empty list — the state while the recipe loads — and
            // PrimaryScrollableTabRow indexes tabPositions[selectedTabIndex] unguarded, so an
            // empty row crashes on measure.
            if (tabs.size > 1) {
                PrimaryScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
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
            IconToggleButton(
                checked = state.recipe.favorite,
                onCheckedChange = {},
                enabled = !state.loading
            ) {
                Icon(Icons.Default.Favorite, contentDescription = "Favorite")
            }
            IconButton(onClick = { /* TODO: edit action */ }, enabled = !state.loading) {
                Icon(Icons.Default.Edit, contentDescription = "Edit")
            }
            IconButton(onClick = { /* TODO: timeline action */ }, enabled = !state.loading) {
                Icon(Icons.Default.ViewTimeline, contentDescription = "Timeline")
            }
            IconButton(
                onClick = { onEvent(RecipeDetailsIntent.ShowSettings) },
                enabled = !state.loading
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
            IconButton(onClick = { /* TODO: more action */ }, enabled = !state.loading) {
                Icon(Icons.Default.MoreVert, contentDescription = "More")
            }
            // No steps to cook means nothing for Cook Mode to show — keep the button hidden
            // rather than opening an empty stepper, same as the compact FAB.
            AnimatedVisibility(
                visible = !state.loading && state.recipe.instructions.isNotEmpty()
            ) {
                Button(
                    onClick = { onStartCooking(state.recipe.id) },
                    shape = CircleShape,
                    contentPadding = PaddingValues(start = 16.dp, end = 20.dp),
                    modifier = Modifier.padding(start = 12.dp).height(40.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize)
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Start cooking", style = MaterialTheme.typography.titleSmall)
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

@Composable
private fun shimmerColors() = listOf(
    MaterialTheme.colorScheme.surfaceContainer,
    MaterialTheme.colorScheme.surfaceContainerHighest,
    MaterialTheme.colorScheme.surfaceContainer,
)
