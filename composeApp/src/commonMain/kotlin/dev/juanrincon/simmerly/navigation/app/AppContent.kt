package dev.juanrincon.simmerly.navigation.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.juanrincon.simmerly.recipes.presentation.decompose.RecipesContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(component: AppComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        val child = it.instance
        val appBarConfig = when(child) {
            is AppComponent.Child.RecipesChild -> child.component.appBarConfig.subscribeAsState().value
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(appBarConfig.title) },
                    actions = {
                        appBarConfig.actions.forEach { action ->
                            IconButton(onClick = action.onClick, content = action.icon)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors().copy(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                )
            },
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = modifier
        ) { paddingValues ->
            when (child) {
                is AppComponent.Child.RecipesChild -> RecipesContent(child.component, modifier = Modifier.padding(paddingValues))
            }
        }
    }
}