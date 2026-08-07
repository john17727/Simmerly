package dev.juanrincon.simmerly.recipes.presentation.details.models

import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.utils.format
import dev.juanrincon.simmerly.recipes.domain.model.Note
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.recipes.domain.model.Tool
import kotlin.math.round

data class RecipeDetailUi(
    val id: String,
    val title: UiText,
    val image: String,
    val description: UiText?,
    val rating: Double?,
    val totalTime: String?,
    val prepTime: String?,
    val performTime: String?,
    val servings: Double,
    val favorite: Boolean,
    val link: String?,
    val tags: List<Tag>,
    val ingredients: List<IngredientUi>,
    val instructions: List<InstructionUi>,
    val tools: List<Tool>,
    val nutrition: NutritionUi,
    val notes: List<Note>,
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
            title = UiText.Dynamic(""),
            image = "",
            description = UiText.Dynamic(""),
            rating = null,
            totalTime = null,
            prepTime = null,
            performTime = null,
            servings = 0.0,
            favorite = false,
            link = null,
            tags = emptyList(),
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
            notes = emptyList(),
            settings = Settings(
                public = true,
                showNutrition = false,
                showAssets = false,
                landscapeView = false,
                disableComments = true,
                locked = false
            )
        )
    }
}

/**
 * Recomputes every ingredient's [IngredientUi.quantity] for a new serving count, scaling
 * proportionally and rounding to two decimal places. [newServings] is clamped to at least 1 — a
 * recipe can't serve zero people. Returns this unchanged (`===`-equal via data class equality) if
 * the clamped value doesn't actually change anything, so callers can cheaply detect a no-op.
 *
 * Shared by [dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsViewModel] and
 * [dev.juanrincon.simmerly.recipes.presentation.cookmode.CookModeViewModel] — both let a cook
 * scale the same recipe, and the scaling rule must stay identical between them.
 */
fun RecipeDetailUi.withServings(newServings: Double): RecipeDetailUi {
    val clamped = newServings.coerceAtLeast(1.0)
    if (clamped == servings) return this

    val factor = clamped / servings
    return copy(
        servings = clamped,
        ingredients = ingredients.map { ingredient ->
            val quantity = ingredient.quantity ?: return@map ingredient
            ingredient.copy(quantity = round(quantity * factor * 100.0) / 100.0)
        }
    )
}
