package dev.juanrincon.simmerly.recipes.presentation

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class RecipesTopBarController {
    var title: @Composable () -> Unit by mutableStateOf({ Text("Recipes") })
    var navigationIcon: @Composable () -> Unit by mutableStateOf({})
    var actions: @Composable RowScope.() -> Unit by mutableStateOf({})

    fun reset() {
        title = { Text("Recipes") }
        navigationIcon = {}
        actions = {}
    }
}

val LocalRecipesTopBarController = compositionLocalOf { RecipesTopBarController() }
