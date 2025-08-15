package dev.juanrincon.simmerly.navigation.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.juanrincon.simmerly.recipes.presentation.decompose.RecipesContent

@Composable
fun AppContent(component: AppComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        when(val child = it.instance) {
            is AppComponent.Child.RecipesChild -> RecipesContent(child.component)
        }
    }
}