package dev.juanrincon.simmerly.navigation.auth

import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import dev.juanrincon.simmerly.navigation.app.AppContent
import dev.juanrincon.simmerly.splash.presentation.SplashScreen
import dev.juanrincon.simmerly.welcome.presentation.decompose.WelcomeContent

@Composable
fun RootContent(component: RootComponent, modifier: Modifier = Modifier, windowSizeClass: WindowSizeClass) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        when(val child = it.instance) {
            is RootComponent.Child.WelcomeChild -> WelcomeContent(child.component, windowSizeClass = windowSizeClass)
            is RootComponent.Child.SplashChild -> SplashScreen()
            is RootComponent.Child.AppChild -> AppContent(child.component)
        }
    }
}
