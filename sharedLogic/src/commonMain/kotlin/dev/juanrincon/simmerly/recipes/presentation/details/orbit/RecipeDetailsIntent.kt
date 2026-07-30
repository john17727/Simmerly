package dev.juanrincon.simmerly.recipes.presentation.details.orbit

import dev.juanrincon.simmerly.recipes.domain.model.Settings

sealed interface RecipeDetailsIntent {
    data object AddServing : RecipeDetailsIntent
    data object RemoveServing : RecipeDetailsIntent
    data object ShowSettings : RecipeDetailsIntent
    data object DismissSettings : RecipeDetailsIntent
    data class UpdateSettings(val settings: Settings) : RecipeDetailsIntent
}
