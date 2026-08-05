package dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit

import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.toCookSteps
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi

enum class CookPhase { MISE_EN_PLACE, STEPS, DONE }

data class NewTimerDraft(
    val minutes: Int = 5,
    val seconds: Int = 0,
    val label: String = ""
)

data class CookModeState(
    val loading: Boolean = true,
    val recipe: RecipeDetailUi = RecipeDetailUi.emptyRecipe,
    val error: RecipesError? = null,
    val phase: CookPhase = CookPhase.MISE_EN_PLACE,
    val stepIndex: Int = 0,
    val checkedIngredientIds: Set<String> = emptySet(),
    val timers: List<CookTimerUi> = emptyList(),
    val notifiedFinishedTimerIds: Set<String> = emptySet(),
    val nowMillis: Long = 0L,
    val cookingStartedAtMillis: Long? = null,
    val cookingFinishedAtMillis: Long? = null,
    val showTimerList: Boolean = false,
    val newTimerDraft: NewTimerDraft? = null,
    val rating: Int = 0,
    val noteDraft: String = ""
) {
    val steps get() = recipe.toCookSteps()

    val currentStep get() = steps.getOrNull(stepIndex)

    val readyIngredientCount get() = recipe.ingredients.count { it.referenceId in checkedIngredientIds }
}
