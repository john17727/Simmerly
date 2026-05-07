package dev.juanrincon.simmerly.recipes.presentation.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SearchBarValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberContainedSearchBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeCard
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeCardSkeleton
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStore
import kotlinx.coroutines.launch
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
    val scope = rememberCoroutineScope()
    val appBarWithSearchColors =
        SearchBarDefaults.appBarWithSearchColors(
            searchBarColors = SearchBarDefaults.containedColors(state = searchBarState)
        )
    val inputField = @Composable {
        SearchBarDefaults.InputField(
            textFieldState = textFieldState,
            searchBarState = searchBarState,
            colors = appBarWithSearchColors.searchBarColors.inputFieldColors,
            onSearch = {
                viewModel.onEvent(
                    RecipeSearchStore.Intent.OnQuerySubmitted(textFieldState.text.toString())
                )
                scope.launch { searchBarState.animateToCollapsed() }
            },
            placeholder = { Text("Search recipes…") },
            leadingIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }

    LaunchedEffect(textFieldState) {
        snapshotFlow { textFieldState.text.toString() }
            .collect { query -> viewModel.onEvent(RecipeSearchStore.Intent.OnQueryChanged(query)) }
    }

    val suggestions = remember(state.recipes, state.searchQuery) {
        if (state.searchQuery.isEmpty()) emptyList()
        else state.recipes.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
    }

    val results = remember(state.recipes, state.submittedQuery) {
        if (state.submittedQuery.isEmpty()) emptyList()
        else state.recipes.filter { it.name.contains(state.submittedQuery, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            AppBarWithSearch(
                state = searchBarState,
                inputField = inputField,
                colors = appBarWithSearchColors,
            )
            ExpandedFullScreenContainedSearchBar(
                state = searchBarState,
                colors = appBarWithSearchColors.searchBarColors,
                inputField = inputField,
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(suggestions, key = { it.id }) { recipe ->
                        ListItem(
                            headlineContent = { Text(recipe.name) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    textFieldState.edit { replace(0, length, recipe.name) }
                                }
                        )
                    }
                }
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            if (state.isLoading && results.isEmpty() && state.submittedQuery.isNotEmpty()) {
                items(6) {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            } else {
                items(results, key = { it.id }) { recipe ->
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
