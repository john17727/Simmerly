package dev.juanrincon.simmerly.navigation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import dev.juanrincon.simmerly.core.presentation.navigation.AppNavRequest
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.profile.presentation.ProfileViewModel
import dev.juanrincon.simmerly.recipes.presentation.RecipesContent
import dev.juanrincon.simmerly.shoppinglists.presentation.ShoppingListsContent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppContent(
    profileViewModel: ProfileViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.RECIPES) }
    var isAtRecipesRoot by rememberSaveable { mutableStateOf(true) }
    var isAtShoppingListsRoot by rememberSaveable { mutableStateOf(true) }
    // A request from one tab for another to handle; that tab consumes it once it is showing.
    var pendingRequest by remember { mutableStateOf<AppNavRequest?>(null) }
    val user by profileViewModel.user.collectAsState()
    val baseNavSuiteType =
        NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())
    // Drilling into a recipe or a list should hide the *bottom bar* on a phone, where the detail
    // screen owns the whole window — but never the rail, which the design keeps alongside it.
    val isRail = baseNavSuiteType in RAIL_TYPES
    val isAtTabRoot = when (currentDestination) {
        AppDestinations.RECIPES -> isAtRecipesRoot
        AppDestinations.SHOPPING_LIST -> isAtShoppingListsRoot
        else -> true
    }
    val effectiveNavSuiteType =
        if (!isAtTabRoot && !isRail) NavigationSuiteType.None else baseNavSuiteType
    val saveableStateHolder = rememberSaveableStateHolder()
    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteType = effectiveNavSuiteType,
        navigationItems = {
            AppDestinations.entries.forEach { destination ->
                NavigationSuiteItem(
                    icon = {
                        if (destination == AppDestinations.PROFILE) {
                            SubcomposeAsyncImage(
                                model = user?.image,
                                contentDescription = destination.contentDescription.asString(),
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = destination.contentDescription.asString(),
                                    )
                                },
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(32.dp).clip(CircleShape),
                            )
                        } else {
                            Icon(
                                painter = destination.icon.painter(),
                                contentDescription = destination.contentDescription.asString(),
                            )
                        }
                    },
                    label = {
                        Text(
                            if (destination == AppDestinations.PROFILE)
                                user?.name ?: destination.label.asString()
                            else
                                destination.label.asString()
                        )
                    },
                    selected = destination == currentDestination,
                    onClick = { currentDestination = destination },
                )
            }
        }
    ) {
        // NavigationSuiteScaffold uses a plain `when` block, which fully removes a destination
        // from the composition tree when switching tabs. Without SaveableStateProvider, all
        // rememberSaveable state (e.g. nested nav back stacks, scroll positions) is destroyed
        // and lost when switching away. SaveableStateProvider snapshots and restores that state
        // per destination, so each tab picks up exactly where it left off.
        saveableStateHolder.SaveableStateProvider(currentDestination) {
            when (val destination = currentDestination) {
                AppDestinations.RECIPES -> RecipesContent(
                    onAtRootChanged = { isAtRecipesRoot = it },
                    onNavigate = { request ->
                        pendingRequest = request
                        currentDestination = when (request) {
                            is AppNavRequest.OpenShoppingList -> AppDestinations.SHOPPING_LIST
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                AppDestinations.MEAL_PLAN -> {
                    Text(destination.label.asString())
                }

                AppDestinations.SHOPPING_LIST -> ShoppingListsContent(
                    onAtRootChanged = { isAtShoppingListsRoot = it },
                    request = pendingRequest as? AppNavRequest.OpenShoppingList,
                    onRequestHandled = { pendingRequest = null },
                    modifier = Modifier.fillMaxSize()
                )

                AppDestinations.PROFILE -> {
                    Text(destination.label.asString())
                }
            }
        }
    }
}

// NavigationSuiteScaffoldDefaults.navigationSuiteType() resolves to WideNavigationRailCollapsed on
// a desktop window; the other two are only reachable if the suite type is ever set explicitly.
private val RAIL_TYPES = setOf(
    NavigationSuiteType.WideNavigationRailCollapsed,
    NavigationSuiteType.WideNavigationRailExpanded,
    NavigationSuiteType.NavigationRail,
)
