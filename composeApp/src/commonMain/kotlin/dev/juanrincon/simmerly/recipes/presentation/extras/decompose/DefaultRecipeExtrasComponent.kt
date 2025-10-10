package dev.juanrincon.simmerly.recipes.presentation.extras.decompose

import com.arkivanov.decompose.ComponentContext
import dev.juanrincon.simmerly.recipes.presentation.decompose.DefaultRecipesComponent
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultRecipeExtrasComponent(
    private val recipeId: String,
    mode: DefaultRecipesComponent.ExtrasMode,
    componentContext: ComponentContext
) : RecipeExtrasComponent {
    private val _state = MutableStateFlow(RecipeExtrasStore.State.Comments(listOf()))
    override val state: StateFlow<RecipeExtrasStore.State> = _state.asStateFlow()


    override fun onEvent(event: RecipeExtrasStore.Intent) {
        TODO("Not yet implemented")
    }
}