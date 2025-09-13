package dev.juanrincon.simmerly.recipes.data.mappers

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
import dev.juanrincon.simmerly.recipes.domain.model.User

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
    associatedIngredients = ingredients.map { it.toDomain() }
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
    locked = locked ?: false
)

fun NoteEntity.toDomain(): Note = Note(
    id = id,
    title = title,
    text = text
)

fun CommentWithRelations.toDomain(): Comment = Comment(
    text = comment.text,
    id = comment.id,
    createdAt = comment.createdAt,
    updatedAt = comment.updatedAt,
    user = user.toDomain()
)

fun UserEntity.toDomain(): User = User(
    id = id,
    username = username,
    admin = admin,
    fullName = fullName
)

fun RecipeDetail.toEntityWithRelations(): RecipeDetailWithRelations = RecipeDetailWithRelations(
    recipe = this.toEntity(),
    categories = categories.map { it.toEntity() },
    tags = tags.map { it.toEntity() },
    tools = tools.map { it.toEntity() },
    ingredients = ingredients.map { it.toEntityWithRelations(id) },
    instructions = instructions.map { it.toEntityWithRelations(this.id) },
    notes = notes.map { it.toEntity(this.id) },
    comments = comments.map { it.toEntityWithRelations(this.id) }
)

fun RecipeDetail.toEntity(): RecipeEntity = RecipeEntity(
    id = id,
    userId = userId,
    householdId = householdId,
    groupId = groupId,
    name = name,
    slug = "",
    image = image,
    servings = servings,
    yieldQuantity = recipeYieldQuantity,
    yield = recipeYield,
    totalTime = totalTime,
    prepTime = prepTime,
    cookTime = cookTime,
    performTime = performTime,
    description = description,
    rating = rating,
    originalUrl = originalUrl,
    dateAdded = dateAdded,
    dateUpdated = dateUpdated,
    createdAt = createdAt,
    updatedAt = updatedAt,
    lastMade = lastMade,
    nutrition = nutrition.toEntity(),
    settings = settings.toEntity()
)

fun Nutrition.toEntity(): NutritionEntity = NutritionEntity(
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

fun Settings.toEntity(): SettingsEntity = SettingsEntity(
    public = public,
    showNutrition = showNutrition,
    showAssets = showAssets,
    landscapeView = landscapeView,
    disableComments = disableComments,
    locked = locked
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id,
    groupId = groupId,
    name = name,
    slug = ""
)

fun Tag.toEntity(): TagEntity = TagEntity(
    id = id,
    groupId = groupId,
    name = name,
    slug = ""
)

fun Tool.toEntity(): ToolEntity = ToolEntity(
    id = id,
    groupId = groupId,
    name = name,
    slug = ""
)

fun Ingredient.toEntityWithRelations(recipeId: String): IngredientWithRelations =
    IngredientWithRelations(
        ingredient = this.toEntity(recipeId),
        unit = unit?.toEntity(),
        food = food?.toEntity()
    )

fun Ingredient.toEntity(recipeId: String): IngredientEntity = IngredientEntity(
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

fun Unit.toEntity(): UnitEntity = UnitEntity(
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

fun Food.toEntity(): FoodEntity = FoodEntity(
    id = id,
    name = name,
    pluralName = pluralName,
    description = description,
    labelId = labelId,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun Instruction.toEntityWithRelations(recipeId: String): InstructionWithRelations = InstructionWithRelations(
    instruction = this.toEntity(recipeId),
    ingredients = associatedIngredients.map { it.toEntityWithRelations(recipeId) }
)

fun Instruction.toEntity(recipeId: String): InstructionEntity = InstructionEntity(
    id = id,
    recipeId = recipeId,
    title = title,
    summary = summary,
    text = text
)

fun Note.toEntity(recipeId: String): NoteEntity = NoteEntity(
    id = id,
    title = title,
    text = text,
    recipeId = recipeId
)

fun Comment.toEntityWithRelations(recipeId: String): CommentWithRelations = CommentWithRelations(
    comment = this.toEntity(recipeId),
    user = user.toEntity()
)

fun Comment.toEntity(recipeId: String): CommentEntity = CommentEntity(
    id = id,
    recipeId = recipeId,
    text = text,
    createdAt = createdAt,
    updatedAt = updatedAt,
    userId = user.id
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    username = username,
    admin = admin,
    fullName = fullName
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