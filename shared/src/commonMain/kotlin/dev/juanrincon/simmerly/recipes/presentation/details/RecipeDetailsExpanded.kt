package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsScreen
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore

val EXPANDED_CARD_PADDING = 16.dp

private const val EXPANDED_TAB_RECIPE = "Recipe"
private const val EXPANDED_TAB_NOTES = "Notes"
private const val EXPANDED_TAB_COMMENTS = "Comments"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ExpandedView(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
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
