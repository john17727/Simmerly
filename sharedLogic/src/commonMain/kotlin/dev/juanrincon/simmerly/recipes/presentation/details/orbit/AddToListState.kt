package dev.juanrincon.simmerly.recipes.presentation.details.orbit

import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter

/** The "Add to shopping list" sheet; it is open while [RecipeDetailsState.addToList] is non-null. */
data class AddToListState(
    val step: AddToListStep = AddToListStep.ChooseList,
    val filter: ListFilter = ListFilter.Mine,
    /** The lists passing [filter]; `null` until the first load. */
    val lists: List<AddToListRowUi>? = null,
    val newListName: String = "",
    val target: AddToListTarget? = null,
    /** Servings the picked amounts are scaled to. */
    val servings: Double = 1.0,
    /** Indices into the recipe's ingredients. */
    val selected: Set<Int> = emptySet(),
    val isSubmitting: Boolean = false,
)

enum class AddToListStep { ChooseList, NewListName, PickIngredients }

sealed interface AddToListTarget {
    val name: String

    data class Existing(val id: String, override val name: String) : AddToListTarget
    data class New(override val name: String) : AddToListTarget
}

data class AddToListRowUi(
    val id: String,
    val name: String,
    val itemCount: Int,
    val alreadyHasRecipe: Boolean,
)
