package dev.juanrincon.simmerly.navigation.auth

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.juanrincon.simmerly.navigation.app.AppComponent
import dev.juanrincon.simmerly.splash.presentation.decompose.SplashComponent
import dev.juanrincon.simmerly.welcome.presentation.decompose.WelcomeComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    // It's possible to pop multiple screens at a time on iOS
    fun onBackClicked(toIndex: Int)

    // Defines all possible child components
    sealed class Child {
        data class WelcomeChild(val component: WelcomeComponent) : Child()
        data class SplashChild(val component: SplashComponent) : Child()
        data class AppChild(val component: AppComponent) : Child()
    }
}