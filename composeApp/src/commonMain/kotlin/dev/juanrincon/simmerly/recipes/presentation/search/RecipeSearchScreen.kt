package dev.juanrincon.simmerly.recipes.presentation.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeCard
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeCardSkeleton
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStore
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun RecipeSearchScreen(
    onRecipeSelected: (String) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: RecipeSearchViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val textFieldState = rememberTextFieldState()
    val searchBarState = rememberContainedSearchBarState(initialValue = SearchBarValue.Expanded)
    val appBarWithSearchColors =
        SearchBarDefaults.appBarWithSearchColors(
            searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
        )

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { query -> viewModel.onEvent(RecipeSearchStore.Intent.OnQueryChanged(query)) }
    }

    val filteredRecipes = remember(state.recipes, state.searchQuery) {
        if (state.searchQuery.isEmpty()) state.recipes
        else state.recipes.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
    }

    ExpandedFullScreenContainedSearchBar(
        state = searchBarState,
        properties = DialogProperties(dismissOnBackPress = false),
        colors = appBarWithSearchColors.searchBarColors,
        modifier = modifier,
        inputField = {
            SearchBarDefaults.InputField(
                textFieldState = textFieldState,
                searchBarState = searchBarState,
                colors = appBarWithSearchColors.searchBarColors.inputFieldColors,
                onSearch = {},
                placeholder = { Text("Search recipes…") },
                leadingIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (state.isLoading && filteredRecipes.isEmpty()) {
                items(6) {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            } else {
                items(filteredRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = { onRecipeSelected(recipe.id) },
                        modifier = Modifier.fillMaxWidth().animateItem()
                    )
                }
            }
        }
    }
}
