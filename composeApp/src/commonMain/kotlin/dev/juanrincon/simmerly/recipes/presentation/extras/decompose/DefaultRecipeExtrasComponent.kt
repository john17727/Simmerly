package dev.juanrincon.simmerly.recipes.presentation.extras.decompose

import com.arkivanov.decompose.ComponentContext
import dev.juanrincon.simmerly.recipes.presentation.decompose.DefaultRecipesComponent
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultRecipeExtrasComponent(
    private val recipeId: String,
    private val mode: DefaultRecipesComponent.ExtraMode,
    componentContext: ComponentContext
) : RecipeExtrasComponent {
    private val _state = MutableStateFlow(initialState(mode))
    override val state: StateFlow<RecipeExtrasStore.State> = _state.asStateFlow()

    override fun onEvent(event: RecipeExtrasStore.Intent) {
        TODO("Not yet implemented")
    }

    private fun initialState(mode: DefaultRecipesComponent.ExtraMode) = when (mode) {
        DefaultRecipesComponent.ExtraMode.COMMENTS -> RecipeExtrasStore.State.Comments(listOf())
        DefaultRecipesComponent.ExtraMode.SETTINGS -> RecipeExtrasStore.State.Settings(false)
    }
}