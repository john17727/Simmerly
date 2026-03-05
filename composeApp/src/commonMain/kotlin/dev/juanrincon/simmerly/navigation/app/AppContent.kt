package dev.juanrincon.simmerly.navigation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteItem
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.recipes.presentation.RecipesContent

@Composable
fun AppContent(modifier: Modifier = Modifier) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.RECIPES) }
    val navSuiteType =
        NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfo())
    val saveableStateHolder = rememberSaveableStateHolder()
    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteType = navSuiteType,
        primaryActionContent = {
            FloatingActionButton(onClick = {}) {
                Icon(Icons.Default.Add, contentDescription = null)
            }
        },
        navigationItems = {
            AppDestinations.entries.forEach {
                NavigationSuiteItem(
                    icon = {
                        Icon(
                            painter = it.icon.painter(),
                            contentDescription = it.contentDescription.asString()
                        )
                    },
                    label = { Text(it.label.asString()) },
                    selected = it == currentDestination,
                    onClick = { currentDestination = it }
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
                AppDestinations.RECIPES -> RecipesContent(modifier = Modifier.fillMaxSize())

                AppDestinations.MEAL_PLAN -> {
                    Text(destination.label.asString())
                }

                AppDestinations.SHOPPING_LIST -> {
                    Text(destination.label.asString())
                }

                AppDestinations.PROFILE -> {
                    Text(destination.label.asString())
                }
            }
        }
    }
}