package dev.juanrincon.simmerly.shoppinglists.domain.model

data class ShoppingListItem(
    val id: String,
    val listId: String,
    val quantity: Double,
    val unit: ShoppingUnit?,
    val food: ShoppingFood?,
    val note: String,
    /** Mealie's preformatted text for the item; empty when the server didn't compute one. */
    val display: String,
    val checked: Boolean,
    val position: Int,
    val labelId: String?,
    val label: ShoppingLabel?,
    val recipeRefs: List<ItemRecipeRef>,
)

/** How much of an item one linked recipe accounts for. */
data class ItemRecipeRef(
    val id: String,
    val recipeId: String,
    val quantity: Double,
    val scale: Double?,
    val note: String?,
)

data class ShoppingUnit(
    val id: String,
    val name: String,
    val pluralName: String?,
    val abbreviation: String,
    val useAbbreviation: Boolean,
)

data class ShoppingFood(
    val id: String,
    val name: String,
    val pluralName: String?,
    val labelId: String?,
    /** The name of the food's label, i.e. its aisle. */
    val labelName: String? = null,
)

/** A Mealie label, which a shopping list treats as an aisle. */
data class ShoppingLabel(
    val id: String,
    val name: String,
    val color: String,
)
