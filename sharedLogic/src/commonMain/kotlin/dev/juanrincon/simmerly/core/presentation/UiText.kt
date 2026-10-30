package dev.juanrincon.simmerly.core.presentation

enum class StringKey {
    LoginFailed,
    SomethingWentWrong,
    UnreachableServerAddress,
    Recipes,
    MealPlan,
    ShoppingList,
    Profile,
    ShoppingListLoadFailed,
    ShoppingListUpdateFailed,
    /** Args: list name. */
    ItemAddedToList,
    /** Args: item count, list name. */
    ItemsAddedToList,
    /** Args: list name. */
    ListCreatedWithItem,
    /** Args: list name, item count. */
    ListCreatedWithItems,
}

sealed interface UiText {
    data class Dynamic(val text: String) : UiText
    data class Resource(val key: StringKey, val args: List<String> = emptyList()) : UiText
}
