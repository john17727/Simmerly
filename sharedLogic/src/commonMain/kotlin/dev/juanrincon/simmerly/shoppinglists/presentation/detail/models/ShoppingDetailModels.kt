package dev.juanrincon.simmerly.shoppinglists.presentation.detail.models

/**
 * One line of the list, already split for display: "[quantityText] **[name]**" with [note]
 * beneath. [nameIsFood] is false for note-only items, which the UI doesn't embolden.
 */
data class ShoppingItemUi(
    val id: String,
    val quantityText: String,
    val name: String,
    val nameIsFood: Boolean,
    val note: String?,
    /** The recipes this item came from, e.g. "Shakshuka (6), Lemon ricotta pancakes (4)". */
    val attribution: String?,
    val checked: Boolean,
)

data class ItemSectionUi(
    val key: String,
    val kind: SectionKind,
    /** The label or recipe name; empty for [SectionKind.NoAisle] and [SectionKind.AddedByHand]. */
    val title: String,
    val items: List<ShoppingItemUi>,
)

enum class SectionKind { Aisle, NoAisle, Recipe, AddedByHand }

enum class ItemGrouping { ByAisle, ByRecipe }

data class LinkedRecipeUi(
    val recipeId: String,
    val name: String,
    val imageUrl: String,
    val quantity: Int,
    val itemCount: Int,
)

data class ItemEditorUi(
    val itemId: String,
    val quantity: String,
    val unitId: String?,
    val foodQuery: String,
    val food: FoodOptionUi?,
    val note: String,
    val isSaving: Boolean = false,
)

data class FoodOptionUi(
    val id: String,
    val name: String,
)

data class UnitOptionUi(
    val id: String,
    val name: String,
)
