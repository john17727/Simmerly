package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.domain.model.Note
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun RecipeDetailsScreen(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    AnimatedContent(state.mode) { mode ->
        when (mode) {
            RecipeDetailsStore.RecipeMode.READ_ONLY -> {
                AnimatedContent(
                    windowSizeClass.isWidthAtLeastBreakpoint(
                        WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
                    )
                ) { isExpanded ->
                    if (isExpanded) {
                        ExpandedView(
                            state = state,
                            onEvent = onEvent,
                            modifier.padding(start = 16.dp)
                        )
                    } else {
                        CompactView()
                    }
                }
            }

            RecipeDetailsStore.RecipeMode.EDIT -> TODO()
        }
    }
}

@Composable
private fun CompactView() {

}

@Composable
private fun ExpandedView(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(EXPANDED_CARD_PADDING)
    ) {
        Column(
            modifier = Modifier.fillMaxHeight().width(325.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IngredientAndToolView(
                recipe = state.recipe,
                onRemoveServingButtonClick = { onEvent(RecipeDetailsStore.Intent.RemoveServing) },
                onAddServingButtonClick = { onEvent(RecipeDetailsStore.Intent.AddServing) },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.medium
                )
                    .ifTrue(state.loading) {
                        fillMaxHeight().shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }.ifTrue(state.recipe.settings.showNutrition) {
                        weight(1f)
                    }
            )
            AnimatedVisibility(state.recipe.settings.showNutrition) {
                NutritionView(
                    state.recipe.nutrition, modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.medium
                    ).wrapContentHeight(unbounded = true) // allow shrinking to content height
                        .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxHeight().weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InstructionView(
                instructions = state.recipe.instructions,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.medium
                ).weight(1f)
            )
            if (state.recipe.notes.isNotEmpty()) {
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    NotesView(
                        state.recipe.notes,
                        modifier = Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.medium
                        ).wrapContentHeight(unbounded = true) // allow shrinking to content height
                            .heightIn(max = maxHeight / 3) // but never exceed half of the parent
                    )
                }
            }
        }
    }
}

@Composable
fun NotesView(notes: List<Note>, modifier: Modifier = Modifier) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier
    ) {
        stickyHeader {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text("Notes", style = MaterialTheme.typography.headlineMedium)
            }
        }
        items(notes, key = { it.id }) { note ->
            NoteEntry(note, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun NoteEntry(note: Note, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (note.title.isNotEmpty()) {
            Text(note.title, style = MaterialTheme.typography.titleLarge)
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
        Text("Nutrition", style = MaterialTheme.typography.headlineMedium)
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
        Text(value)
    }
}

@Composable
private fun IngredientAndToolView(
    recipe: RecipeDetailUi,
    onAddServingButtonClick: () -> Unit,
    onRemoveServingButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        stickyHeader {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text("Ingredients", style = MaterialTheme.typography.headlineMedium)
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
        }
        items(recipe.ingredients) { ingredient ->
            IngredientEntry(ingredient, modifier = Modifier.fillMaxWidth())
        }
        if (recipe.tools.isNotEmpty()) {
            item {
                Text(
                    "Tools",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
        items(recipe.tools) { tool ->
            Text(tool.name, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun InstructionView(
    instructions: List<InstructionUi>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        modifier = modifier
    ) {
        stickyHeader {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surfaceContainer)
                    .padding(top = 16.dp, bottom = 8.dp)
            ) {
                Text("Instructions", style = MaterialTheme.typography.headlineMedium)
            }
        }
        items(instructions) { instruction ->
            InstructionEntry(instruction, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun IngredientEntry(ingredient: IngredientUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.fillMaxWidth(0.6f)) {
            Text(ingredient.formattedDisplay)
            ingredient.note?.let { note ->
                Text(
                    note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
        ingredient.formattedQuantity?.let {
            Text(it, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
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
        Text(instruction.title, style = MaterialTheme.typography.headlineSmall)
        Row {
            instruction.associatedIngredients.forEach {
                Text(it.formattedDisplay, style = MaterialTheme.typography.bodySmall)
            }
        }
        RichText(
            state = richTextState,
//        imageLoader = Coil3ImageLoader, // TODO: Add image loader
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

val EXPANDED_CARD_PADDING = 16.dp

@Preview
@Composable
fun ExpandedViewPreview() {
//    ExpandedView(state = RecipeDetail(), modifier = Modifier.fillMaxSize())
}
