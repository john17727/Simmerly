package dev.juanrincon.simmerly.shoppinglists.domain.model

/**
 * Free text split by Mealie's ingredient parser. [unit] and [food] are only set when they match
 * ones that already exist; otherwise just their parsed names ([unitName], [foodName]) are known.
 */
data class ParsedIngredient(
    val quantity: Double?,
    val unit: ShoppingUnit?,
    val unitName: String?,
    val food: ShoppingFood?,
    val foodName: String?,
    val note: String,
)
