package dev.juanrincon.simmerly.shoppinglists.presentation.detail.models

import dev.juanrincon.simmerly.core.presentation.UiText

/**
 * One line of the list, already split for display: "[quantityText] **[name]**" with [note]
 * beneath. [nameIsFood] is false for note-only items, which the UI doesn't embolden.
 */
data class ShoppingItemUi(
    val id: String,
    val quantityText: UiText,
    val name: UiText,
    val nameIsFood: Boolean,
    val note: UiText?,
    /** The recipes this item came from, e.g. "Shakshuka (6), Lemon ricotta pancakes (4)". */
    val attribution: UiText?,
    val checked: Boolean,
)

data class ItemSectionUi(
    val key: String,
    val kind: SectionKind,
    /** The label or recipe name; empty for [SectionKind.NoAisle] and [SectionKind.AddedByHand]. */
    val title: UiText,
    val items: List<ShoppingItemUi>,
)

enum class SectionKind { Aisle, NoAisle, Recipe, AddedByHand }

enum class ItemGrouping { ByAisle, ByRecipe }

data class LinkedRecipeUi(
    val recipeId: String,
    val name: UiText,
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
    val name: UiText,
    /** The food's aisle, or `null` when it has none. */
    val aisle: UiText? = null,
)

data class UnitOptionUi(
    val id: String,
    val name: UiText,
)
