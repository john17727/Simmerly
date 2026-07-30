package dev.juanrincon.simmerly.core

import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NutritionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.SettingsEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentSearchQueryEntity
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentlyViewedEntity
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
fun aRecipeEntity(
    id: String = "recipe-1",
    name: String = "Test Recipe",
    createdAt: Instant = Clock.System.now()
) = RecipeEntity(
    id = id,
    userId = "user-1",
    householdId = "household-1",
    groupId = "group-1",
    name = name,
    slug = id,
    image = "",
    servings = 4.0,
    yieldQuantity = 4.0,
    yield = "",
    totalTime = "30 minutes",
    prepTime = null,
    cookTime = null,
    performTime = null,
    description = "",
    rating = null,
    originalUrl = "",
    dateAdded = createdAt,
    dateUpdated = createdAt,
    createdAt = createdAt,
    updatedAt = createdAt,
    lastMade = null,
    nutrition = NutritionEntity.empty(),
    settings = SettingsEntity.empty()
)

@OptIn(ExperimentalTime::class)
fun aCommentEntity(
    id: String = "comment-1",
    recipeId: String = "recipe-1",
    text: String = "Great recipe!",
    userId: String = "user-1",
    createdAt: Instant = Clock.System.now()
) = CommentEntity(
    id = id,
    recipeId = recipeId,
    text = text,
    createdAt = createdAt,
    updatedAt = createdAt,
    userId = userId
)

fun aUserEntity(
    id: String = "user-1",
    username: String = "john",
    fullName: String = "John Doe"
) = UserEntity(
    id = id,
    username = username,
    admin = false,
    fullName = fullName
)

fun aRecentSearchQueryEntity(
    query: String = "pasta",
    searchedAt: Long = System.currentTimeMillis()
) = RecentSearchQueryEntity(
    query = query,
    searchedAt = searchedAt
)

fun aRecentlyViewedEntity(
    recipeId: String = "recipe-1",
    viewedAt: Long = System.currentTimeMillis()
) = RecentlyViewedEntity(
    recipeId = recipeId,
    viewedAt = viewedAt
)
