package dev.juanrincon.simmerly.navigation.app

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.value.Value
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.list.decompose.DefaultRecipeListComponent
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class DefaultAppComponent(
    componentContext: ComponentContext,
    val storeFactory: StoreFactory,
) : AppComponent,
    ComponentContext by componentContext, KoinComponent {

    private val navigation = StackNavigation<AppConfiguration>()

    override val stack: Value<ChildStack<*, AppComponent.Child>> = childStack(
        source = navigation,
        serializer = AppConfiguration.serializer(),
        initialConfiguration = AppConfiguration.Recipes,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(
        config: AppConfiguration,
        componentContext: ComponentContext
    ): AppComponent.Child = when (config) {
        AppConfiguration.Recipes -> AppComponent.Child.RecipesChild(
            DefaultRecipeListComponent(
                componentContext,
                storeFactory,
                get<RecipeRepository>()
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