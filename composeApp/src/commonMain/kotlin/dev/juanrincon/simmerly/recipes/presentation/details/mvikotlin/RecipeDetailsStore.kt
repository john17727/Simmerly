package dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State

interface RecipeDetailsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object AddServing : Intent
        data object RemoveServing : Intent
        data object ShowSettings: Intent
        data object DismissSettings: Intent
    }

    data class State(
        val loading: Boolean = false,
        val recipe: RecipeDetailUi = RecipeDetailUi.emptyRecipe,
        val mode: RecipeMode = RecipeMode.READ_ONLY,
        val showSettings: Boolean = false,
    )

    sealed interface Label {
    }

    enum class RecipeMode {
        READ_ONLY,
        EDIT
    }
}
