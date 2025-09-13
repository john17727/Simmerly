package dev.juanrincon.simmerly.recipes.presentation.details.models

import dev.juanrincon.simmerly.core.utils.format
import dev.juanrincon.simmerly.recipes.domain.model.Settings

data class RecipeDetailUi(
    val id: String,
    val title: String,
    val image: String,
    val servings: Double,
    val ingredients: List<IngredientUi>,
    val settings: Settings
) {

    val formattedServings = if (servings > 1.0) {
        "${servings.format(1)} servings"
    } else {
        "${servings.format(1)} serving"
    }

    companion object {
        val emptyRecipe = RecipeDetailUi(
            id = "",
            title = "",
            image = "",
            servings = 0.0,
            ingredients = emptyList(),
            settings = Settings(
                public = true,
                showNutrition = false,
                showAssets = false,
                landscapeView = false,
                disableComments = false,
                disableAmount = true,
                locked = false
            )
        )
    }
}
