package dev.juanrincon.simmerly.recipes.presentation

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import dev.juanrincon.simmerly.core.presentation.navigation.AppNavRequest
import dev.juanrincon.simmerly.core.presentation.navigation.BottomSheetSceneStrategy
import dev.juanrincon.simmerly.core.presentation.navigation.PaneWidths
import dev.juanrincon.simmerly.core.presentation.navigation.rememberTwoPaneDirective
import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsPanel
import dev.juanrincon.simmerly.recipes.presentation.cookmode.CookModeScreen
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsScreen
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListScreen
import dev.juanrincon.simmerly.recipes.presentation.navigation.RecipeDestinations
import dev.juanrincon.simmerly.recipes.presentation.search.RecipeSearchScreen
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

@OptIn(
    ExperimentalMaterial3AdaptiveApi::class,
    ExperimentalMaterial3Api::class,
    ExperimentalSharedTransitionApi::class, ExperimentalSerializationApi::class
)
@Composable
fun RecipesContent(
    onAtRootChanged: (Boolean) -> Unit = {},
    onNavigate: (AppNavRequest) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<RecipeDestinations>()
                }
            }
        },
        RecipeDestinations.List
    )

    val commentsOpen = backStack.any { it is RecipeDestinations.Comments }
    val isAtRoot by remember { derivedStateOf { backStack.size <= 1 } }
    // The open recipe is whatever Detail destination is on the back stack. Deriving it here, rather
    // than keeping a copy in the list's ViewModel, keeps it in step with the detail pane: the back
    // stack is saved across tab switches, but the ViewModel is cleared with the list entry.
    val selectedRecipeId = backStack.filterIsInstance<RecipeDestinations.Detail>()
        .lastOrNull()?.recipeId.orEmpty()
    LaunchedEffect(isAtRoot) { onAtRootChanged(isAtRoot) }

    SharedTransitionLayout {
        NavDisplay(
            backStack = backStack,
            // BottomSheetSceneStrategy is an overlay strategy and must run before any non-overlay
            // one (ListDetailSceneStrategy here) — see its KDoc.
            sceneStrategies = listOf(BottomSheetSceneStrategy(), rememberListDetailSceneStrategy(directive = rememberTwoPaneDirective(extraPaneOpen = commentsOpen))),
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
                        selectedRecipeId = selectedRecipeId,
                        modifier = Modifier.fillMaxSize(),
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onSearchClicked = { backStack.add(RecipeDestinations.Search) },
                        onRecipeSelected = { recipeId ->
                            backStack.removeAll { it is RecipeDestinations.Detail || it is RecipeDestinations.Comments }
                            backStack.add(RecipeDestinations.Detail(recipeId))
                        }
                    )
                }
                entry<RecipeDestinations.Search> {
                    RecipeSearchScreen(
                        onRecipeSelected = { recipeId ->
                            backStack.removeAll { it is RecipeDestinations.Detail || it is RecipeDestinations.Comments }
                            backStack.add(RecipeDestinations.Detail(recipeId))
                        },
                        onNavigateBack = { backStack.removeLastOrNull() },
                        sharedTransitionScope = this@SharedTransitionLayout,
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<RecipeDestinations.Detail>(
                    metadata = ListDetailSceneStrategy.detailPane()
                ) { key ->
                    RecipeDetailsScreen(
                        recipeId = key.recipeId,
                        sharedTransitionScope = this@SharedTransitionLayout,
                        onNavigateBack = { backStack.removeLastOrNull() },
                        commentsOpen = commentsOpen,
                        onToggleComments = { id ->
                            // The header's Comments button toggles the extra pane on desktop.
                            if (backStack.lastOrNull() is RecipeDestinations.Comments) {
                                backStack.removeLastOrNull()
                            } else {
                                backStack.add(RecipeDestinations.Comments(id))
                            }
                        },
                        onStartCooking = { backStack.add(RecipeDestinations.CookMode(it)) },
                        onNavigateToShoppingList = { onNavigate(AppNavRequest.OpenShoppingList(it)) },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                entry<RecipeDestinations.CookMode>(
                    metadata = BottomSheetSceneStrategy.bottomSheet()
                ) { key ->
                    CookModeScreen(
                        recipeId = key.recipeId,
                        onExit = { backStack.removeLastOrNull() },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                // The extra pane: beside the detail pane where the window has room (400dp, as in
                // the desktop design), a full-screen replacement elsewhere. Same sheet either way.
                entry<RecipeDestinations.Comments>(
                    metadata = ListDetailSceneStrategy.extraPane() +
                        ListDetailSceneStrategy.preferredPaneSize(width = PaneWidths.Comments)
                ) { key ->
                    RecipeCommentsPanel(
                        recipeId = key.recipeId,
                        onClose = { backStack.removeLastOrNull() },
                        // The activity resizes for the keyboard rather than panning, so the
                        // composer has to lift itself above it.
                        modifier = Modifier.fillMaxSize().imePadding()
                    )
                }
            }
        )
    } // SharedTransitionLayout

}