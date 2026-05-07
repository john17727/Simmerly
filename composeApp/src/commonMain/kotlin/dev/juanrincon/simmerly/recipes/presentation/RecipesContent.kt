package dev.juanrincon.simmerly.recipes.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsScreen
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsScreen
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListScreen
import dev.juanrincon.simmerly.recipes.presentation.navigation.RecipeDestinations
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RecipesContent(
    onAtRootChanged: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(RecipeDestinations.List::class, RecipeDestinations.List.serializer())
                    subclass(
                        RecipeDestinations.Detail::class,
                        RecipeDestinations.Detail.serializer()
                    )
                    subclass(
                        RecipeDestinations.Comments::class,
                        RecipeDestinations.Comments.serializer()
                    )
                }
            }
        },
        RecipeDestinations.List
    )

    val isAtRoot by remember { derivedStateOf { backStack.size <= 1 } }
    LaunchedEffect(isAtRoot) { onAtRootChanged(isAtRoot) }

    NavDisplay(
        backStack = backStack,
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        modifier = modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<RecipeDestinations.List>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a recipe to see details")
                        }
                    }
                )
            ) {
                RecipeListScreen(
                    modifier = Modifier.fillMaxSize(),
                    onRecipeSelected = { recipeId ->
                        backStack.removeAll { it is RecipeDestinations.Detail }
                        backStack.add(RecipeDestinations.Detail(recipeId))
                    }
                )
            }
            entry<RecipeDestinations.Detail>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                RecipeDetailsScreen(
                    recipeId = key.recipeId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    onNavigateToComments = { backStack.add(RecipeDestinations.Comments(it)) },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<RecipeDestinations.Comments>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) { key ->
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text("Comments") },
                            navigationIcon = {
                                IconButton(onClick = { backStack.removeLastOrNull() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Back"
                                    )
                                }
                            },
                            colors = TopAppBarDefaults.topAppBarColors()
                                .copy(containerColor = MaterialTheme.colorScheme.background)
                        )
                    }
                ) { paddingValues ->
                    RecipeCommentsScreen(
                        recipeId = key.recipeId,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    )

}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
private fun RecipesNavDisplay(modifier: Modifier = Modifier) {

}