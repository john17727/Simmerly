package dev.juanrincon.simmerly.shoppinglists.domain.model

data class ShoppingList(
    val id: String,
    val name: String,
    val userId: String,
    val items: List<ShoppingListItem>,
    val recipes: List<LinkedRecipe>,
    /** Label ids in the order this list sorts its aisles. */
    val labelOrder: List<String>,
)

/** A recipe added to a list [quantity] times — 2 means its ingredients were added twice. */
data class LinkedRecipe(
    val recipeId: String,
    val name: String,
    val slug: String,
    val imageUrl: String,
    val quantity: Double,
)
