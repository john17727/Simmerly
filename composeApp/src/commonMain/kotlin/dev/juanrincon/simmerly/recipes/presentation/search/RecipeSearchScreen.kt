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
import androidx.compose.material.icons.automirrored.rounded.ArrowBackIos
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AppBarWithSearch
import androidx.compose.material3.ExpandedFullScreenContainedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
    val searchBarState = rememberContainedSearchBarState()
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
                if (searchBarState.currentValue == SearchBarValue.Expanded) {
                    IconButton(onClick = { scope.launch { searchBarState.animateToCollapsed() } }) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBackIos,
                            contentDescription = "Dismiss search"
                        )
                    }
                } else {
                    Icon(Icons.Rounded.Search, contentDescription = null)
                }
            },
            trailingIcon = if (textFieldState.text.isNotEmpty()) {
                {
                    IconButton(onClick = {
                        textFieldState.edit { replace(0, length, "") }
                        viewModel.onEvent(RecipeSearchStore.Intent.OnQuerySubmitted(""))
                    }) {
                        Icon(Icons.Rounded.Close, contentDescription = "Clear")
                    }
                }
            } else null
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
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
            ExpandedFullScreenContainedSearchBar(
                state = searchBarState,
                colors = appBarWithSearchColors.searchBarColors,
                inputField = inputField,
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    if (state.searchQuery.isEmpty()) {
                        items(state.recentQueries, key = { it }) { query ->
                            ListItem(
                                headlineContent = { Text(query) },
                                leadingContent = {
                                    Icon(Icons.Rounded.History, contentDescription = null)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        textFieldState.edit { replace(0, length, query) }
                                    }
                            )
                        }
                    } else {
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
            }
        },
        modifier = modifier
    ) { paddingValues ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            if (state.submittedQuery.isEmpty()) {
                if (state.recentRecipes.isNotEmpty()) {
                    item {
                        Text(
                            text = "Recently Viewed",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
                items(state.recentRecipes, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = {
                            viewModel.onEvent(RecipeSearchStore.Intent.OnRecipeViewed(recipe.id))
                            onRecipeSelected(recipe.id)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else if (state.isLoading && results.isEmpty()) {
                items(6) {
                    RecipeCardSkeleton(modifier = Modifier.fillMaxWidth())
                }
            } else {
                items(results, key = { it.id }) { recipe ->
                    RecipeCard(
                        recipe = recipe,
                        onClick = {
                            viewModel.onEvent(RecipeSearchStore.Intent.OnRecipeViewed(recipe.id))
                            onRecipeSelected(recipe.id)
                        },
                        modifier = Modifier.fillMaxWidth().animateItem()
                    )
                }
            }
        }
    }
}
