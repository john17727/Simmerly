package dev.juanrincon.simmerly.recipes.presentation.details.mappers

import dev.juanrincon.simmerly.core.utils.capitalizeWords
import dev.juanrincon.simmerly.core.utils.nullIfEmpty
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.Food
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.domain.model.Instruction
import dev.juanrincon.simmerly.recipes.domain.model.Nutrition
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.Unit
import dev.juanrincon.simmerly.recipes.presentation.comments.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.FoodUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.UnitUi

fun RecipeDetail.toRecipeDetailUi(): RecipeDetailUi = RecipeDetailUi(
    id = id,
    title = name,
    image = image,
    servings = servings,
    ingredients = ingredients.map { it.toIngredientUi() },
    instructions = instructions.mapIndexed { index, instruction -> instruction.toInstructionUi(index + 1) },
    nutrition = nutrition.toNutritionUi(),
    tools = tools,
    notes = notes,
    settings = settings
)

fun Ingredient.toIngredientUi(): IngredientUi = IngredientUi(
    quantity = quantity,
    display = display,
    food = food?.toFoodUi(),
    unit = unit?.toUnitUi(),
    note = if (display == note) null else note?.nullIfEmpty()
)

fun Food.toFoodUi(): FoodUi = FoodUi(
    name = name,
    pluralName = pluralName
)

fun Unit.toUnitUi(): UnitUi = UnitUi(
    name = name,
    pluralName = pluralName,
    fraction = fraction,
    abbreviation = abbreviation,
    pluralAbbreviation = pluralAbbreviation,
    useAbbreviation = useAbbreviation
)

fun Instruction.toInstructionUi(step: Int): InstructionUi = InstructionUi(
    id = id,
    title = title.nullIfEmpty(),
    summary = formatInstructionStep(summary, step),
    text = text,
    associatedIngredients = associatedIngredients.map { it.toIngredientUi() }
)

fun Nutrition.toNutritionUi(): NutritionUi = NutritionUi(
    calories = calories,
    carbohydrateContent = formatToGrams(carbohydrateContent),
    cholesterolContent = formatToMilligrams(cholesterolContent),
    fatContent = formatToGrams(fatContent),
    fiberContent = formatToGrams(fiberContent),
    proteinContent = formatToGrams(proteinContent),
    saturatedFatContent = formatToGrams(saturatedFatContent),
    sodiumContent = formatToMilligrams(sodiumContent),
    sugarContent = formatToGrams(sugarContent),
    transFatContent = formatToGrams(transFatContent),
    unsaturatedFatContent = formatToGrams(unsaturatedFatContent)
)

fun Comment.toCommentUi(): CommentUi = CommentUi(
    id = id,
    text = text,
    user = user.fullName,
    isUserAuthor = false // TODO: Implement ability to map if user is author
)


    private fun formatToGrams(nutrition: String?): String? = nutrition?.let {
        "$it grams"
    }

            private fun formatToMilligrams(nutrition: String?): String? = nutrition?.let {
        "$it milligrams"
    }

            private fun formatInstructionStep(title: String, step: Int): String =
        if (title.isBlank()) {
            "Step $step"
        } else {
            title.capitalizeWords()
        }