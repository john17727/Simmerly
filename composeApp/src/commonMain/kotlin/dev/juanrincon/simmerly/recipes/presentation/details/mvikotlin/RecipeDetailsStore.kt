package dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State

interface RecipeDetailsStore : Store<Intent, State, Label> {

    sealed interface Intent {
    }

    data class State(
        val loading: Boolean = false,
        val recipe: RecipeDetailUi = RecipeDetailUi.emptyRecipe,
        val mode: RecipeMode = RecipeMode.READ_ONLY
    )

    sealed interface Label {
    }

    enum class RecipeMode {
        READ_ONLY,
        EDIT
    }
}
