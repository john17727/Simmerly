package dev.juanrincon.simmerly.recipes

import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.model.Comment
import dev.juanrincon.simmerly.recipes.domain.model.Nutrition
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.domain.model.User
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

fun aRecipeSummary(
    id: String = "recipe-1",
    name: String = "Pasta"
) = RecipeSummary(
    id = id,
    name = name,
    image = "",
    tags = emptyList(),
    rating = null,
    totalTime = null,
    prepTime = null,
    performTime = null,
    description = ""
)

fun aSettings(
    showNutrition: Boolean = false,
    disableComments: Boolean = false
) = Settings(
    public = true,
    showNutrition = showNutrition,
    showAssets = false,
    landscapeView = false,
    disableComments = disableComments,
    locked = false
)

fun aRecipeDetailUi(
    id: String = "test-recipe",
    servings: Double = 4.0,
    settings: Settings = aSettings(),
    ingredients: List<IngredientUi> = emptyList(),
) = RecipeDetailUi.emptyRecipe.copy(
    id = id,
    servings = servings,
    settings = settings,
    ingredients = ingredients
)

fun anIngredientUi(quantity: Double = 100.0) = IngredientUi(
    quantity = quantity,
    display = "$quantity g pasta",
    food = null,
    unit = null,
    note = null
)

@OptIn(ExperimentalTime::class)
fun aRecipeDetail(id: String = "test-recipe", servings: Double = 4.0) = RecipeDetail(
    id = id,
    userId = "",
    householdId = "",
    groupId = "",
    name = "Test Recipe",
    image = "",
    servings = servings,
    recipeYieldQuantity = servings,
    recipeYield = "",
    totalTime = "",
    prepTime = null,
    cookTime = null,
    performTime = null,
    description = "",
    categories = emptyList(),
    tags = emptyList(),
    tools = emptyList(),
    rating = null,
    originalUrl = "",
    dateAdded = Clock.System.now(),
    dateUpdated = Clock.System.now(),
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    lastMade = null,
    ingredients = emptyList(),
    instructions = emptyList(),
    nutrition = Nutrition(
        calories = null,
        carbohydrateContent = null,
        cholesterolContent = null,
        fatContent = null,
        fiberContent = null,
        proteinContent = null,
        saturatedFatContent = null,
        sodiumContent = null,
        sugarContent = null,
        transFatContent = null,
        unsaturatedFatContent = null
    ),
    settings = aSettings(),
    assets = emptyList(),
    notes = emptyList(),
    comments = emptyList()
)

@OptIn(ExperimentalTime::class)
fun aComment(
    id: String = "comment-1",
    text: String = "Hello",
    authorName: String = "John Doe"
) = Comment(
    id = id,
    text = text,
    createdAt = Clock.System.now(),
    updatedAt = Clock.System.now(),
    user = User(id = "user-1", username = "john", admin = false, fullName = authorName, image = "")
)
