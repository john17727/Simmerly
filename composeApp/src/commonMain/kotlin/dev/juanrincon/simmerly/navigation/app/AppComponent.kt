package dev.juanrincon.simmerly.navigation.app

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.value.Value
import dev.juanrincon.simmerly.recipes.presentation.decompose.RecipesComponent

interface AppComponent {
    val stack: Value<ChildStack<*, Child>>

    // It's possible to pop multiple screens at a time on iOS
    fun onBackClicked(toIndex: Int)

    // Defines all possible child components
    sealed class Child {
        data class RecipesChild(val component: RecipesComponent) : Child()
    }
}