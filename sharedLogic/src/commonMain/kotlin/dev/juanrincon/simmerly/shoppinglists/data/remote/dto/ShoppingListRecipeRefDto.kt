package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import kotlinx.serialization.Serializable

/** A recipe added to a list, with how many times it was added ([recipeQuantity]). */
@Serializable
data class ShoppingListRecipeRefDto(
    val id: String,
    val shoppingListId: String,
    val recipeId: String,
    val recipeQuantity: Double,
    val recipe: ShoppingRecipeSummaryDto,
)

/** The few fields of Mealie's `RecipeSummary` a shopping list needs. */
@Serializable
data class ShoppingRecipeSummaryDto(
    val id: String? = null,
    val name: String? = null,
    val slug: String = "",
)
