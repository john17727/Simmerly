package dev.juanrincon.simmerly.navigation.app

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.recipes.presentation.RecipesContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(modifier: Modifier = Modifier) {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.RECIPES) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentDestination.label.asString()) },
//                    actions = {
//                        appBarConfig.actions.forEach { action ->
//                            IconButton(onClick = action.onClick, content = action.icon)
//                        }
//                    },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        modifier = modifier
    ) { paddingValues ->
        NavigationSuiteScaffold(
            modifier = Modifier.padding(paddingValues),
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
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