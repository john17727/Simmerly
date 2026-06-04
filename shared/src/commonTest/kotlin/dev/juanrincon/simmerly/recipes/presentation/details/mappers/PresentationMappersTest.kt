package dev.juanrincon.simmerly.recipes.presentation.details.mappers

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.recipes.domain.model.Food
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.domain.model.Instruction
import dev.juanrincon.simmerly.recipes.domain.model.Nutrition
import dev.juanrincon.simmerly.recipes.domain.model.Unit
import dev.juanrincon.simmerly.recipes.aRecipeDetail
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class PresentationMappersTest {

    // region RecipeDetail.toRecipeDetailUi

    @Test
    fun recipeDetailToRecipeDetailUiMapsIdAndTitle() {
        val detail = aRecipeDetail(id = "recipe-123")

        val ui = detail.toRecipeDetailUi()

        assertThat(ui.id).isEqualTo("recipe-123")
    }

    @Test
    fun recipeDetailToRecipeDetailUiMapsServings() {
        val detail = aRecipeDetail(servings = 6.0)

        val ui = detail.toRecipeDetailUi()

        assertThat(ui.servings).isEqualTo(6.0)
    }

    @Test
    fun recipeDetailToRecipeDetailUiMapsIngredientsInOrder() {
        val detail = aRecipeDetail().copy(
            ingredients = listOf(
                anIngredient(display = "200g flour"),
                anIngredient(display = "1 cup milk")
            )
        )

        val ui = detail.toRecipeDetailUi()

        assertThat(ui.ingredients.size).isEqualTo(2)
        assertThat(ui.ingredients[0].display).isEqualTo("200g flour")
        assertThat(ui.ingredients[1].display).isEqualTo("1 cup milk")
    }

    @Test
    fun recipeDetailToRecipeDetailUiAddsStepNumbersToInstructions() {
        val detail = aRecipeDetail().copy(
            instructions = listOf(
                anInstruction(summary = ""),
                anInstruction(summary = "")
            )
        )

        val ui = detail.toRecipeDetailUi()

        assertThat(ui.instructions[0].summary).isEqualTo("Step 1")
        assertThat(ui.instructions[1].summary).isEqualTo("Step 2")
    }

    @Test
    fun recipeDetailToRecipeDetailUiUsesSummaryAsStepWhenPresent() {
        val detail = aRecipeDetail().copy(
            instructions = listOf(anInstruction(summary = "mix the flour"))
        )

        val ui = detail.toRecipeDetailUi()

        assertThat(ui.instructions[0].summary).isEqualTo("Mix The Flour")
    }

    // endregion

    // region Ingredient.toIngredientUi

    @Test
    fun ingredientToIngredientUiMapsQuantityAndDisplay() {
        val ingredient = anIngredient(quantity = 200.0, display = "200g pasta")

        val ui = ingredient.toIngredientUi()

        assertThat(ui.quantity).isEqualTo(200.0)
        assertThat(ui.display).isEqualTo("200g pasta")
    }

    @Test
    fun ingredientToIngredientUiSetsNoteNullWhenNoteEqualsDisplay() {
        val ingredient = anIngredient(display = "200g pasta", note = "200g pasta")

        val ui = ingredient.toIngredientUi()

        assertThat(ui.note).isNull()
    }

    @Test
    fun ingredientToIngredientUiPreservesNoteWhenDifferentFromDisplay() {
        val ingredient = anIngredient(display = "200g pasta", note = "al dente")

        val ui = ingredient.toIngredientUi()

        assertThat(ui.note).isEqualTo("al dente")
    }

    @Test
    fun ingredientToIngredientUiSetsNoteNullWhenNoteIsEmpty() {
        val ingredient = anIngredient(display = "200g pasta", note = "")

        val ui = ingredient.toIngredientUi()

        assertThat(ui.note).isNull()
    }

    // endregion

    // region Instruction.toInstructionUi

    @Test
    fun instructionToInstructionUiWithBlankSummaryUsesStepNumber() {
        val instruction = anInstruction(summary = "  ")

        val ui = instruction.toInstructionUi(step = 3)

        assertThat(ui.summary).isEqualTo("Step 3")
    }

    @Test
    fun instructionToInstructionUiCapitalizesSummary() {
        val instruction = anInstruction(summary = "preheat the oven")

        val ui = instruction.toInstructionUi(step = 1)

        assertThat(ui.summary).isEqualTo("Preheat The Oven")
    }

    @Test
    fun instructionToInstructionUiMapsIdAndText() {
        val instruction = anInstruction(id = "step-1", text = "Mix well")

        val ui = instruction.toInstructionUi(step = 1)

        assertThat(ui.id).isEqualTo("step-1")
        assertThat(ui.text).isEqualTo("Mix well")
    }

    @Test
    fun instructionToInstructionUiWithEmptyTitleMapsNullTitle() {
        val instruction = anInstruction(title = "")

        val ui = instruction.toInstructionUi(step = 1)

        assertThat(ui.title).isNull()
    }

    // endregion

    // region Nutrition.toNutritionUi

    @Test
    fun nutritionToNutritionUiFormatsCaloriesUnchanged() {
        val nutrition = aNutrition(calories = "250")

        val ui = nutrition.toNutritionUi()

        assertThat(ui.calories).isEqualTo("250")
    }

    @Test
    fun nutritionToNutritionUiFormatsCarbohydratesToGrams() {
        val nutrition = aNutrition(carbohydrateContent = "30")

        val ui = nutrition.toNutritionUi()

        assertThat(ui.carbohydrateContent).isEqualTo("30 grams")
    }

    @Test
    fun nutritionToNutritionUiFormatsSodiumToMilligrams() {
        val nutrition = aNutrition(sodiumContent = "400")

        val ui = nutrition.toNutritionUi()

        assertThat(ui.sodiumContent).isEqualTo("400 milligrams")
    }

    @Test
    fun nutritionToNutritionUiWithNullValuesProducesNulls() {
        val nutrition = aNutrition(carbohydrateContent = null, fatContent = null)

        val ui = nutrition.toNutritionUi()

        assertThat(ui.carbohydrateContent).isNull()
        assertThat(ui.fatContent).isNull()
    }

    // endregion

    // region Helpers

    private fun anIngredient(
        quantity: Double = 100.0,
        display: String = "100g pasta",
        note: String? = null
    ) = Ingredient(
        quantity = quantity,
        unit = null,
        food = Food(id = "food-1", name = "pasta", pluralName = "pastas", description = "", labelId = null, createdAt = Clock.System.now(), updatedAt = Clock.System.now()),
        note = note,
        display = display,
        title = null,
        originalText = display,
        referenceId = "ref-1"
    )

    private fun anInstruction(
        id: String = "instr-1",
        title: String = "",
        summary: String = "Step summary",
        text: String = "Do something"
    ) = Instruction(
        id = id,
        title = title,
        summary = summary,
        text = text,
        associatedIngredients = emptyList()
    )

    private fun aNutrition(
        calories: String? = null,
        carbohydrateContent: String? = null,
        fatContent: String? = null,
        sodiumContent: String? = null
    ) = Nutrition(
        calories = calories,
        carbohydrateContent = carbohydrateContent,
        cholesterolContent = null,
        fatContent = fatContent,
        fiberContent = null,
        proteinContent = null,
        saturatedFatContent = null,
        sodiumContent = sodiumContent,
        sugarContent = null,
        transFatContent = null,
        unsaturatedFatContent = null
    )

    // endregion
}
