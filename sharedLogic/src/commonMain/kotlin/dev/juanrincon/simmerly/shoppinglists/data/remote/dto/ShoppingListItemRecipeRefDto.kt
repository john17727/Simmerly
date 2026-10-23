package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import kotlinx.serialization.Serializable

/** Ties a list item back to the recipe that contributed it, and how much of it that recipe needs. */
@Serializable
data class ShoppingListItemRecipeRefDto(
    val id: String,
    val shoppingListItemId: String,
    val recipeId: String,
    val recipeQuantity: Double = 0.0,
    val recipeScale: Double? = 1.0,
    val recipeNote: String? = null,
)
