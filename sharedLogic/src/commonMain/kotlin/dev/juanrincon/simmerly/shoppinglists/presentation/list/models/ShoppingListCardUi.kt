package dev.juanrincon.simmerly.shoppinglists.presentation.list.models

data class ShoppingListCardUi(
    val id: String,
    val name: String,
    val ownerId: String,
    val itemCount: Int,
    val checkedCount: Int,
    /** Share of items checked, from 0 to 1. */
    val progress: Float,
    val footer: ListCardFooterUi,
)

data class RecipeThumbUi(
    val recipeId: String,
    val name: String,
    val imageUrl: String,
)

/** The bottom of a list card, which depends on where the list's items came from. */
sealed interface ListCardFooterUi {
    /** The list has no items yet. */
    data object Empty : ListCardFooterUi

    /** Every item comes from one recipe, added [quantity] times. */
    data class OneRecipe(
        val recipe: RecipeThumbUi,
        val itemCount: Int,
        val quantity: Int,
    ) : ListCardFooterUi

    /** Every item comes from recipes; [recipes] holds the first few for the avatar stack. */
    data class SeveralRecipes(
        val recipes: List<RecipeThumbUi>,
        val recipeCount: Int,
        val recipeNames: List<String>,
    ) : ListCardFooterUi

    /** No item comes from a recipe; [itemNames] previews the first few. */
    data class AddedByHand(
        val itemNames: List<String>,
        val itemCount: Int,
    ) : ListCardFooterUi

    /** A mix of recipe items and items added by hand. */
    data class RecipesAndExtras(
        val recipes: List<RecipeThumbUi>,
        val recipeCount: Int,
        val recipeNames: List<String>,
        val fromRecipesCount: Int,
        val byHandCount: Int,
    ) : ListCardFooterUi
}
