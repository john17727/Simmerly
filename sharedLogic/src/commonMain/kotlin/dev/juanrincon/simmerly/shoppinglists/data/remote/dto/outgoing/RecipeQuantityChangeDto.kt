package dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIncrementDto(val recipeIncrementQuantity: Double)

@Serializable
data class RecipeDecrementDto(val recipeDecrementQuantity: Double)
