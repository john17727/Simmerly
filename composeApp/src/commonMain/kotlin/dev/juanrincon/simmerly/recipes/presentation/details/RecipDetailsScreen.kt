package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
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
    AnimatedContent(state::class) {
        when (state) {
            RecipeDetailsStore.State.Loading -> {
                Column(
                    modifier = modifier,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                }
            }

            is RecipeDetailsStore.State.Content -> AnimatedContent(
                windowSizeClass.isWidthAtLeastBreakpoint(
                    WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
                )
            ) { isExpanded ->
                if (isExpanded) {
                    ExpandedView(state.recipe, modifier)
                } else {
                    CompactView()
                }
            }
        }
    }
}

@Composable
private fun ExpandedView(recipe: RecipeDetailUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(start = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(EXPANDED_CARD_PADDING)
    ) {
        Card(
            modifier = Modifier.fillMaxHeight().widthIn(200.dp, 350.dp),
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            IngredientList(recipe.ingredients)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(EXPANDED_CARD_PADDING)
        ) {
            AsyncImage(
                recipe.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().weight(1f).clip(MaterialTheme.shapes.medium)
            )
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                colors = CardDefaults.cardColors()
                    .copy(containerColor = MaterialTheme.colorScheme.surface)
            ) {

            }
        }
    }
}

@Composable
private fun CompactView() {

}

@Composable
private fun IngredientList(ingredients: List<IngredientUi>) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        stickyHeader {
            Text("Ingredients", style = MaterialTheme.typography.headlineMedium)
        }
        items(ingredients) { ingredient ->
            IngredientEntry(ingredient, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun IngredientEntry(ingredient: IngredientUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(ingredient.name)
            ingredient.note?.let { note ->
                Text(note, style = MaterialTheme.typography.bodySmall)
            }
        }
        ingredient.quantity?.let {
            Text(it, fontWeight = FontWeight.Bold)
        }
    }
}

val EXPANDED_CARD_PADDING = 16.dp

@Preview
@Composable
fun ExpandedViewPreview() {
//    ExpandedView(state = RecipeDetail(), modifier = Modifier.fillMaxSize())
}
