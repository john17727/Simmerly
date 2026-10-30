package dev.juanrincon.simmerly.shoppinglists.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
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
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.ShoppingListDetailScreen
import dev.juanrincon.simmerly.shoppinglists.presentation.list.ShoppingListsScreen
import dev.juanrincon.simmerly.shoppinglists.presentation.navigation.ShoppingListDestinations
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** The Shopping List tab: the lists, and a list's items beside them on wide windows or on top of
 * them on a phone. */
@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalSerializationApi::class)
@Composable
fun ShoppingListsContent(
    modifier: Modifier = Modifier,
    onAtRootChanged: (Boolean) -> Unit = {},
    /** A list to open on top of the lists, e.g. from a recipe's "View list" action. */
    openListId: String? = null,
    onOpenListHandled: () -> Unit = {},
) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclassesOfSealed<ShoppingListDestinations>()
                }
            }
        },
        ShoppingListDestinations.Lists
    )

    val isAtRoot by remember { derivedStateOf { backStack.size <= 1 } }
    LaunchedEffect(isAtRoot) { onAtRootChanged(isAtRoot) }
    LaunchedEffect(openListId) {
        if (openListId != null) {
            backStack.removeAll { it is ShoppingListDestinations.Detail }
            backStack.add(ShoppingListDestinations.Detail(openListId))
            onOpenListHandled()
        }
    }

    NavDisplay(
        backStack = backStack,
        sceneStrategies = listOf(rememberListDetailSceneStrategy()),
        modifier = modifier.fillMaxSize(),
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<ShoppingListDestinations.Lists>(
                metadata = ListDetailSceneStrategy.listPane(
                    detailPlaceholder = {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Select a list to see its items")
                        }
                    }
                )
            ) {
                ShoppingListsScreen(
                    selectedListId = (backStack.lastOrNull() as? ShoppingListDestinations.Detail)?.listId,
                    onListSelected = { listId ->
                        backStack.removeAll { it is ShoppingListDestinations.Detail }
                        backStack.add(ShoppingListDestinations.Detail(listId))
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
            entry<ShoppingListDestinations.Detail>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                ShoppingListDetailScreen(
                    listId = key.listId,
                    onNavigateBack = { backStack.removeLastOrNull() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    )
}
