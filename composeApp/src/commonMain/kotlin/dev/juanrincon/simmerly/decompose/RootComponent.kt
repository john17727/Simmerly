package dev.juanrincon.simmerly.decompose

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.juanrincon.simmerly.welcome.presentation.decompose.WelcomeComponent

interface RootComponent {
    val stack: Value<ChildStack<*, Child>>

    // It's possible to pop multiple screens at a time on iOS
    fun onBackClicked(toIndex: Int)

    // Defines all possible child components
    sealed class Child {
        data class WelcomeChild(val component: WelcomeComponent) : Child()
    }
}