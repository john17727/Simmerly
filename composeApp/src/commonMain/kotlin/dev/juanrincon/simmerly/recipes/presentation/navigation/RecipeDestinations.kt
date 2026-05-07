package dev.juanrincon.simmerly.recipes.presentation.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface RecipeDestinations: NavKey {
    @Serializable
    data object List : RecipeDestinations
    @Serializable
    data class Detail(val recipeId: String) : RecipeDestinations
    @Serializable
    data class Comments(val recipeId: String) : RecipeDestinations

    @Serializable
    data object Search : RecipeDestinations
}
