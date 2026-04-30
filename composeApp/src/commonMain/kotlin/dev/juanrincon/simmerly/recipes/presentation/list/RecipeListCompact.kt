package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.theme.SimmerlyTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun CompactRecipeList(
    recipes: List<RecipeSummary>,
    isLoading: Boolean,
    onRecipeSelected: (String) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Recipes") },
                scrollBehavior = scrollBehavior
            )
        },
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) { paddingValues ->
        RecipeList(
            recipes = recipes,
            isLoading = isLoading,
            onRecipeSelected = onRecipeSelected,
            lazyListState = lazyListState,
            modifier = modifier.padding(paddingValues)
        )
    }
}

@Composable
private fun RecipeList(
    recipes: List<RecipeSummary>,
    isLoading: Boolean,
    onRecipeSelected: (String) -> Unit,
    lazyListState: LazyListState,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        state = lazyListState,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.clip(shape = MaterialTheme.shapes.medium)
    ) {
        if (isLoading && recipes.isEmpty()) {
            items(6) {
                RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
            }
        } else {
            items(recipes, key = { it.id }) { item ->
                RecipeCard(
                    item,
                    onClick = { onRecipeSelected(item.id) },
                    modifier = Modifier.fillMaxWidth().animateItem()
                )
            }
            if (isLoading) {
                item {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            }
        }
    }
}

private val previewRecipes = listOf(
    RecipeSummary(
        id = "1",
        name = "Spaghetti Carbonara",
        image = "",
        tags = listOf(Tag(id = "1", groupId = "cuisine", name = "Italian")),
        rating = 4.8,
        totalTime = "30 min",
        prepTime = "10 min",
        performTime = "20 min",
        description = "A classic Roman pasta dish with eggs, guanciale, and Pecorino Romano."
    ),
    RecipeSummary(
        id = "2",
        name = "Chicken Tikka Masala",
        image = "",
        tags = listOf(Tag(id = "2", groupId = "cuisine", name = "Indian")),
        rating = 4.5,
        totalTime = "45 min",
        prepTime = "15 min",
        performTime = "30 min",
        description = "Tender chicken in a rich, spiced tomato cream sauce."
    ),
    RecipeSummary(
        id = "3",
        name = "Avocado Toast",
        image = "",
        tags = listOf(Tag(id = "3", groupId = "meal", name = "Breakfast")),
        rating = 4.1,
        totalTime = "10 min",
        prepTime = "5 min",
        performTime = "5 min",
        description = "Creamy smashed avocado on sourdough with chili flakes and lemon."
    ),
    RecipeSummary(
        id = "4",
        name = "Beef Tacos",
        image = "",
        tags = listOf(Tag(id = "4", groupId = "cuisine", name = "Mexican")),
        rating = 4.6,
        totalTime = "25 min",
        prepTime = "10 min",
        performTime = "15 min",
        description = "Crispy shells filled with seasoned beef, salsa, and fresh toppings."
    ),
)

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicPreview() {
    SimmerlyTheme(dynamicColor = true) {
        CompactRecipeList(
            recipes = previewRecipes,
            isLoading = false,
            onRecipeSelected = {},
            lazyListState = LazyListState()
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun LightPreview() {
    SimmerlyTheme {
        CompactRecipeList(
            recipes = previewRecipes,
            isLoading = false,
            onRecipeSelected = {},
            lazyListState = LazyListState()
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicDarkPreview() {
    SimmerlyTheme(dynamicColor = true, darkTheme = true) {
        CompactRecipeList(
            recipes = previewRecipes,
            isLoading = false,
            onRecipeSelected = {},
            lazyListState = LazyListState()
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun DarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        CompactRecipeList(
            recipes = previewRecipes,
            isLoading = false,
            onRecipeSelected = {},
            lazyListState = LazyListState()
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun LoadingPreview() {
    SimmerlyTheme {
        CompactRecipeList(
            recipes = emptyList(),
            isLoading = true,
            onRecipeSelected = {},
            lazyListState = LazyListState()
        )
    }
}
