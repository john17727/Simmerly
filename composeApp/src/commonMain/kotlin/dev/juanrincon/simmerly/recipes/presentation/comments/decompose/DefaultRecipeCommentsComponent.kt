package dev.juanrincon.simmerly.recipes.presentation.comments.decompose

import com.arkivanov.decompose.ComponentContext
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeExtrasStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DefaultRecipeCommentsComponent(
    private val recipeId: String,
    componentContext: ComponentContext
) : RecipeCommentsComponent {
    private val _state = MutableStateFlow(RecipeExtrasStore.State())
    override val state: StateFlow<RecipeExtrasStore.State> = _state.asStateFlow()

    override fun onEvent(event: RecipeExtrasStore.Intent) {
        TODO("Not yet implemented")
    }
}