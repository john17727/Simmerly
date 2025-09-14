package dev.juanrincon.simmerly.recipes.presentation.details.models

import dev.juanrincon.simmerly.core.utils.format
import dev.juanrincon.simmerly.recipes.domain.model.Nutrition
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.domain.model.Tool

data class RecipeDetailUi(
    val id: String,
    val title: String,
    val image: String,
    val servings: Double,
    val ingredients: List<IngredientUi>,
    val instructions: List<InstructionUi>,
    val tools: List<Tool>,
    val nutrition: NutritionUi,
    val settings: Settings
) {

    val isParsed = ingredients.any { it.food != null }

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
            instructions = emptyList(),
            nutrition = NutritionUi(
                calories = "",
                carbohydrateContent = "",
                cholesterolContent = "",
                fatContent = "",
                fiberContent = "",
                proteinContent = "",
                saturatedFatContent = "",
                sodiumContent = "",
                sugarContent = "",
                transFatContent = "",
                unsaturatedFatContent = ""
            ),
            tools = emptyList(),
            settings = Settings(
                public = true,
                showNutrition = false,
                showAssets = false,
                landscapeView = false,
                disableComments = false,
                locked = false
            )
        )
    }
}
