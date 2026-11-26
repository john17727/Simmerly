package dev.juanrincon.simmerly.core.presentation.navigation

/**
 * A request for the app shell to go somewhere in another tab. Tabs keep their own back stacks, so
 * one tab asks with one of these rather than reaching into another's navigation; the shell switches
 * tabs and hands the request to the tab that handles it.
 */
sealed interface AppNavRequest {
    /** Open a shopping list's items in the Shopping List tab. */
    data class OpenShoppingList(val listId: String) : AppNavRequest

    /** Open a recipe's details in the Recipes tab, e.g. from a meal plan card. */
    data class OpenRecipe(val recipeId: String) : AppNavRequest

    /** Open the Meal plan tab on [date], e.g. from "View plan" after adding a recipe. */
    data class OpenMealPlan(val date: kotlinx.datetime.LocalDate) : AppNavRequest
}
