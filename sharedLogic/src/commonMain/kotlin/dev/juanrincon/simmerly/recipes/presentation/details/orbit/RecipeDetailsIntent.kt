package dev.juanrincon.simmerly.recipes.presentation.details.orbit

import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter

sealed interface RecipeDetailsIntent {
    data object AddServing : RecipeDetailsIntent
    data object RemoveServing : RecipeDetailsIntent
    data object ShowSettings : RecipeDetailsIntent
    data object DismissSettings : RecipeDetailsIntent
    data class UpdateSettings(val settings: Settings) : RecipeDetailsIntent

    data object OpenAddToList : RecipeDetailsIntent
    data object DismissAddToList : RecipeDetailsIntent
    data object AddToListBack : RecipeDetailsIntent
    data class AddToListFilterChanged(val filter: ListFilter) : RecipeDetailsIntent
    data class AddToListListChosen(val listId: String) : RecipeDetailsIntent
    data object AddToListNewListClicked : RecipeDetailsIntent
    data class AddToListNameChanged(val name: String) : RecipeDetailsIntent
    data object AddToListNameConfirmed : RecipeDetailsIntent
    data object AddToListAddServing : RecipeDetailsIntent
    data object AddToListRemoveServing : RecipeDetailsIntent
    data class AddToListToggleIngredient(val index: Int) : RecipeDetailsIntent
    data object AddToListToggleAll : RecipeDetailsIntent
    data object AddToListConfirm : RecipeDetailsIntent
}
