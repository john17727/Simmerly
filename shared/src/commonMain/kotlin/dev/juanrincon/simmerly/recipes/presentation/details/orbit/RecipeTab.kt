package dev.juanrincon.simmerly.recipes.presentation.details.orbit

enum class RecipeTab {
    Overview,
    Ingredients,
    Instructions,
    Notes,
    Nutrition,
    Recipe,
    Comments,
}

val RecipeTab.label: String
    get() = when (this) {
        RecipeTab.Overview -> "Overview"
        RecipeTab.Ingredients -> "Ingredients"
        RecipeTab.Instructions -> "Instructions"
        RecipeTab.Notes -> "Notes"
        RecipeTab.Nutrition -> "Nutrition"
        RecipeTab.Recipe -> "Recipe"
        RecipeTab.Comments -> "Comments"
    }
