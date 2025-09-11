package dev.juanrincon.simmerly.recipes.data.mappers

import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.FoodEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NoteEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NutritionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.SettingsEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.ToolEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UnitEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.IngredientWithRelations
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.InstructionWithRelations
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.RecipeDetailWithRelations
import dev.juanrincon.simmerly.recipes.domain.model.Category
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.Food
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.domain.model.Instruction
import dev.juanrincon.simmerly.recipes.domain.model.Note
import dev.juanrincon.simmerly.recipes.domain.model.Nutrition
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.domain.model.Tag
import dev.juanrincon.simmerly.recipes.domain.model.Tool
import dev.juanrincon.simmerly.recipes.domain.model.Unit

fun RecipeEntity.toDomain(host: String?): RecipeSummary = RecipeSummary(
    id = id,
    name = name,
    image = createRecipeImageUrl(host, id),
    tags = listOf(),
    rating = rating,
    cookTime = abbreviateTime(totalTime)
)

fun RecipeDetailWithRelations.toDomain(host: String?): RecipeDetail = RecipeDetail(
    id = recipe.id,
    userId = recipe.userId,
    householdId = recipe.householdId,
    groupId = recipe.groupId,
    name = recipe.name,
    image = createRecipeImageUrl(host, recipe.id),
    servings = recipe.servings,
    recipeYieldQuantity = recipe.yieldQuantity,
    recipeYield = recipe.yield,
    totalTime = recipe.totalTime,
    prepTime = recipe.prepTime,
    cookTime = recipe.cookTime,
    performTime = recipe.performTime,
    description = recipe.description,
    categories = categories.map { it.toDomain() },
    tags = tags.map { it.toDomain() },
    tools = tools.map { it.toDomain() },
    rating = recipe.rating,
    originalUrl = recipe.originalUrl,
    dateAdded = recipe.dateAdded,
    dateUpdated = recipe.dateUpdated,
    createdAt = recipe.createdAt,
    updatedAt = recipe.updatedAt,
    lastMade = recipe.lastMade,
    ingredients = ingredients.map { it.toDomain() },
    instructions = instructions.map { it.toDomain() },
    nutrition = recipe.nutrition.toDomain(),
    settings = recipe.settings.toDomain(),
    assets = listOf(),
    notes = notes.map { it.toDomain() },
    comments = comments.map { it.toDomain() }
)

fun CategoryEntity.toDomain(): Category = Category(
    id = id,
    groupId = groupId,
    name = name
)

fun TagEntity.toDomain(): Tag = Tag(
    id = id,
    groupId = groupId,
    name = name
)

fun ToolEntity.toDomain(): Tool = Tool(
    id = id,
    groupId = groupId,
    name = name
)

fun IngredientWithRelations.toDomain(): Ingredient = Ingredient(
    quantity = ingredient.quantity,
    unit = unit?.toDomain(),
    food = food?.toDomain(),
    note = ingredient.note,
    isFood = ingredient.isFood,
    disableAmount = ingredient.disableAmount,
    display = ingredient.display,
    title = ingredient.title,
    originalText = ingredient.originalText,
    referenceId = ingredient.id
)

fun UnitEntity.toDomain(): Unit = Unit(
    id = id,
    name = name,
    pluralName = pluralName,
    description = description,
    fraction = fraction,
    abbreviation = abbreviation,
    pluralAbbreviation = pluralAbbreviation,
    useAbbreviation = useAbbreviation,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FoodEntity.toDomain(): Food = Food(
    id = id,
    name = name,
    pluralName = pluralName,
    description = description,
    labelId = labelId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun InstructionWithRelations.toDomain(): Instruction = Instruction(
    id = instruction.id,
    title = instruction.title,
    summary = instruction.summary,
    text = instruction.text,
    associatedIngredients = ingredients.map { it.toDomain().food?.name ?: "" }
)

fun NutritionEntity.toDomain(): Nutrition = Nutrition(
    calories = calories,
    carbohydrateContent = carbohydrates,
    cholesterolContent = cholesterol,
    fatContent = fat,
    fiberContent = fiber,
    proteinContent = protein,
    saturatedFatContent = saturatedFat,
    sodiumContent = sodium,
    sugarContent = sugar,
    transFatContent = transFat,
    unsaturatedFatContent = unsaturatedFat
)

fun SettingsEntity.toDomain(): Settings = Settings(
    public = public ?: true,
    showNutrition = showNutrition ?: false,
    showAssets = showAssets ?: false,
    landscapeView = landscapeView ?: false,
    disableComments = disableComments ?: false,
    disableAmount = disableAmount ?: true,
    locked = locked ?: false
)

fun NoteEntity.toDomain(): Note = Note(
    title = title,
    text = text
)

fun CommentEntity.toDomain(): Comment = Comment(
    text = text,
    id = id,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

private fun createRecipeImageUrl(host: String?, id: String): String {
    if (host == null) return ""
    return "$host/api/media/recipes/$id/images/original.webp"
}

private fun abbreviateTime(time: String): String = when {
    time.lowercase().contains("hour") -> time.replace("hour", "h")
    time.lowercase().contains("minute") -> time.replace("minutes", "min")
    else -> time
}