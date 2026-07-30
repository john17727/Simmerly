package dev.juanrincon.simmerly.core.presentation

enum class StringKey {
    LoginFailed,
    SomethingWentWrong,
    UnreachableServerAddress,
    Recipes,
    MealPlan,
    ShoppingList,
    Profile,
}

sealed interface UiText {
    data class Dynamic(val text: String) : UiText
    data class Resource(val key: StringKey, val args: List<String> = emptyList()) : UiText
}
