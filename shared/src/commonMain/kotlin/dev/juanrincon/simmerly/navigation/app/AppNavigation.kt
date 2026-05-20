package dev.juanrincon.simmerly.navigation.app

import kotlinx.serialization.Serializable

@Serializable
sealed class Recipes {
    @Serializable
    data object List : Recipes()

    @Serializable
    data class Detail(val recipeId: String) : Recipes()
}