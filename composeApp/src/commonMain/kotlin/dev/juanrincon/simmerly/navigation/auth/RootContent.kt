package dev.juanrincon.simmerly.navigation.auth

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.juanrincon.simmerly.navigation.app.AppContent
import dev.juanrincon.simmerly.splash.presentation.SplashScreen

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.WelcomeChild -> Unit
            is RootComponent.Child.SplashChild -> SplashScreen()
            is RootComponent.Child.AppChild -> AppContent(child.component)
        }
    }
}
