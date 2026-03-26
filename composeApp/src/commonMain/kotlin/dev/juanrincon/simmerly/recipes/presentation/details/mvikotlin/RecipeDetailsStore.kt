package dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin

import com.arkivanov.mvikotlin.core.store.Store
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State

interface RecipeDetailsStore : Store<Intent, State, Label> {

    sealed interface Intent {
        data object AddServing : Intent
        data object RemoveServing : Intent
        data object ShowSettings : Intent
        data object DismissSettings : Intent
        data class UpdateSettings(val settings: Settings) : Intent
    }

    data class State(
        val loading: Boolean = true,
        val recipe: RecipeDetailUi = RecipeDetailUi.emptyRecipe,
        val mobileTabs: List<String> = listOf(),
        val desktopTabs: List<String> = listOf(),
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
