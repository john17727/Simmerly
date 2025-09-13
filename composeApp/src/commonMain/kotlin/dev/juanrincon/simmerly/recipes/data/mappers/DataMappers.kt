package dev.juanrincon.simmerly.recipes.data.mappers

import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.core.domain.dateStringToInstant
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.FoodEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.InstructionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NoteEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NutritionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.SettingsEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.ToolEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UnitEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.CommentWithRelations
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.IngredientWithRelations
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.InstructionWithRelations
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.RecipeDetailWithRelations
import dev.juanrincon.simmerly.recipes.data.remote.dto.CategoryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.CommentDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.FoodDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.IngredientDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.InstructionDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.NoteDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.NutritionDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeDetailDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.SettingsDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.TagDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.ToolDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.UnitDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.UserDto
import dev.juanrincon.simmerly.recipes.domain.model.PaginationData
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun RecipeSummaryDto.toEntity() = RecipeEntity(
    id = id,
    userId = userId,
    householdId = householdId,
    groupId = groupId,
    name = name,
    slug = slug,
    image = image,
    servings = recipeServings,
    yieldQuantity = recipeYieldQuantity,
    yield = recipeYield,
    totalTime = totalTime,
    prepTime = prepTime,
    cookTime = cookTime,
    performTime = performTime,
    description = description,
    rating = rating,
    originalUrl = orgURL,
    dateAdded = dateStringToInstant(dateAdded),
    dateUpdated = Instant.parse(dateUpdated),
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
    lastMade = lastMade?.let { dateStringToInstant(it) },
    nutrition = NutritionEntity.empty(),
    settings = SettingsEntity.empty()
)

fun ItemListDto<*>.toPaginationData() = PaginationData(
    page = page,
    perPage = perPage,
    total = total,
    totalPages = totalPages,
    next = next,
    previous = previous
)

fun RecipeDetailDto.toEntityWithRelations(): RecipeDetailWithRelations {
    val ingredients = recipeIngredient.map { it.toEntityWithRelations(this.id) }
    return RecipeDetailWithRelations(
        recipe = this.toEntity(),
        categories = recipeCategory.map { it.toEntity() },
        tags = tags.map { it.toEntity() },
        tools = tools.map { it.toEntity() },
        ingredients = ingredients,
        instructions = recipeInstructions.map { it.toEntityWithRelations(this.id, ingredients) },
        notes = notes.map { it.toEntity(this.id) },
        comments = comments.map { it.toEntityWithRelations() }
    )
}

fun RecipeDetailDto.toEntity() = RecipeEntity(
    id = id,
    userId = userId,
    householdId = householdId,
    groupId = groupId,
    name = name,
    slug = slug,
    image = image,
    servings = recipeServings,
    yieldQuantity = recipeYieldQuantity,
    yield = recipeYield,
    totalTime = totalTime,
    prepTime = prepTime,
    cookTime = cookTime,
    performTime = performTime,
    description = description,
    rating = rating,
    originalUrl = orgURL,
    dateAdded = dateStringToInstant(dateAdded),
    dateUpdated = Instant.parse(dateUpdated),
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
    lastMade = lastMade?.let { dateStringToInstant(it) },
    nutrition = nutrition.toEntity(),
    settings = settings.toEntity()
)

fun NutritionDto.toEntity() = NutritionEntity(
    calories = calories,
    carbohydrates = carbohydrateContent,
    cholesterol = cholesterolContent,
    fat = fatContent,
    fiber = fiberContent,
    protein = proteinContent,
    saturatedFat = saturatedFatContent,
    sodium = sodiumContent,
    sugar = sugarContent,
    transFat = transFatContent,
    unsaturatedFat = unsaturatedFatContent
)

fun SettingsDto.toEntity() = SettingsEntity(
    public = public,
    showNutrition = showNutrition,
    showAssets = showAssets,
    landscapeView = landscapeView,
    disableComments = disableComments,
    locked = locked
)

// TODO: Migrate these to their own mappers once their feature is implemented
fun CategoryDto.toEntity() = CategoryEntity(
    id = id,
    groupId = groupId,
    name = name,
    slug = slug
)

fun TagDto.toEntity() = TagEntity(
    id = id,
    groupId = groupId,
    name = name,
    slug = slug
)

fun ToolDto.toEntity() = ToolEntity(
    id = id,
    groupId = groupId,
    name = name,
    slug = slug
)

fun IngredientDto.toEntityWithRelations(recipeId: String) = IngredientWithRelations(
    ingredient = this.toEntity(recipeId),
    unit = unit?.toEntity(),
    food = food?.toEntity()
)

fun IngredientDto.toEntity(recipeId: String) = IngredientEntity(
    id = referenceId,
    recipeId = recipeId,
    quantity = quantity,
    unitId = unit?.id,
    foodId = food?.id,
    note = note,
    display = display,
    title = title,
    originalText = originalText
)

fun UnitDto.toEntity() = UnitEntity(
    id = id,
    name = name,
    pluralName = pluralName,
    description = description,
    fraction = fraction,
    abbreviation = abbreviation,
    pluralAbbreviation = pluralAbbreviation,
    useAbbreviation = useAbbreviation,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt)
)

fun FoodDto.toEntity() = FoodEntity(
    id = id,
    name = name,
    pluralName = pluralName,
    description = description,
    labelId = labelId,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt)
)

fun InstructionDto.toEntityWithRelations(
    recipeId: String,
    ingredients: List<IngredientWithRelations>
) = InstructionWithRelations(
    instruction = this.toEntity(recipeId),
    ingredients = ingredientReferences.map { reference -> ingredients.find { it.ingredient.id == reference.referenceId }!! } // TODO: Hacky, revisit later o
)

fun InstructionDto.toEntity(recipeId: String) = InstructionEntity(
    id = id,
    recipeId = recipeId,
    title = title,
    summary = summary,
    text = text,
)

fun NoteDto.toEntity(recipeId: String) = NoteEntity(
    id = 0,
    title = title,
    text = text,
    recipeId = recipeId
)

fun CommentDto.toEntityWithRelations() = CommentWithRelations(
    comment = this.toEntity(),
    user = user.toEntity()
)

fun CommentDto.toEntity() = CommentEntity(
    id = id,
    recipeId = recipeId,
    text = text,
    createdAt = Instant.parse(createdAt),
    updatedAt = Instant.parse(updatedAt),
    userId = userId
)

fun UserDto.toEntity() = UserEntity(
    id = id,
    username = username,
    admin = admin,
    fullName = fullName
)