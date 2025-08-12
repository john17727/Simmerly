package dev.juanrincon.simmerly.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.juanrincon.simmerly.welcome.presentation.decompose.DefaultWelcomeComponent
import dev.juanrincon.simmerly.welcome.presentation.decompose.WelcomeComponent
import kotlinx.serialization.Serializable

class DefaultRootComponent(componentContext: ComponentContext, val storeFactory: StoreFactory) : RootComponent,
    ComponentContext by componentContext {

        private val navigation = StackNavigation<Configuration>()

    override val stack: Value<ChildStack<*, RootComponent.Child>>
        get() = childStack(
            source = navigation,
            serializer = Configuration.serializer(),
            initialConfiguration = Configuration.Welcome,
            handleBackButton = true,
            childFactory = ::createChild
        )

    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(toIndex)
    }

    private fun createChild(config: Configuration, componentContext: ComponentContext): RootComponent.Child =
        when (config) {
            Configuration.Welcome -> RootComponent.Child.WelcomeChild(welcomeComponent(componentContext))
        }

    private fun welcomeComponent(componentContext: ComponentContext): WelcomeComponent =
        DefaultWelcomeComponent(componentContext, storeFactory)

    @Serializable
    private sealed interface Configuration {
        @Serializable
        data object Welcome : Configuration
    }
}