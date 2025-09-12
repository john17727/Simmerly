package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowSizeClass
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore
import dev.juanrincon.simmerly.theme.Simmerly.Card
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
private fun ExpandedView(recipe: RecipeDetail, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Card(
            modifier = Modifier.fillMaxHeight(),
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            IngredientList(recipe.ingredients, recipe.settings.disableAmount)
        }
    }
}

@Composable
private fun CompactView() {

}

@Composable
private fun IngredientList(ingredients: List<Ingredient>, amountsDisabled: Boolean) {
    ingredients.forEach {
        if (amountsDisabled) {
            Text(it.display)
        } else {
            Row {
                Text(it.quantity.toString())
                Text(it.unit.toString())
                Text(it.food?.name ?: it.display)
            }
        }
    }
}

@Preview
@Composable
fun ExpandedViewPreview() {
//    ExpandedView(state = RecipeDetail(), modifier = Modifier.fillMaxSize())
}
