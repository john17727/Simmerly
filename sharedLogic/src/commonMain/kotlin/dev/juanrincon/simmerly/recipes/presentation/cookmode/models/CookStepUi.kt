package dev.juanrincon.simmerly.recipes.presentation.cookmode.models

import dev.juanrincon.simmerly.recipes.domain.ParsedDuration
import dev.juanrincon.simmerly.recipes.domain.parseDurations
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi

/** One instruction resolved for Cook Mode: its position, its chip-ready ingredients, and any
 * durations detected in its body text. */
data class CookStepUi(
    val index: Int,
    val instruction: InstructionUi,
    val ingredients: List<IngredientUi>,
    val detectedDurations: List<ParsedDuration>
)

/** Builds the ordered list of cook steps from a loaded recipe. Empty for a recipe with no
 * instructions — callers must handle that case (see [RecipeDetailUi.instructions]). */
fun RecipeDetailUi.toCookSteps(): List<CookStepUi> {
    val ingredientsById = ingredients.associateBy { it.referenceId }
    return instructions.mapIndexed { index, instruction ->
        CookStepUi(
            index = index,
            instruction = instruction,
            ingredients = instruction.ingredientIds.mapNotNull { ingredientsById[it] },
            detectedDurations = parseDurations(instruction.text)
        )
    }
}
