package dev.juanrincon.simmerly.navigation.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.value.Value
import dev.juanrincon.simmerly.navigation.auth.RootComponent
import dev.juanrincon.simmerly.recipes.presentation.decompose.DefaultRecipesComponent
import kotlinx.serialization.Serializable

class DefaultAppComponent(componentContext: ComponentContext) : AppComponent,
    ComponentContext by componentContext {

        private val navigation = StackNavigation<AppConfiguration>()

    override val stack: Value<ChildStack<*, AppComponent.Child>> = childStack(
        source = navigation,
        serializer = AppConfiguration.serializer(),
        initialConfiguration = AppConfiguration.Recipes,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(config: AppConfiguration, componentContext: ComponentContext): AppComponent.Child = when(config) {
        AppConfiguration.Recipes -> AppComponent.Child.RecipesChild(
            DefaultRecipesComponent(
                componentContext
            )
        )
    }

    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(toIndex)
    }

    @Serializable
    sealed interface AppConfiguration {
        @Serializable
        data object Recipes : AppConfiguration
    }
}