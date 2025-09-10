package dev.juanrincon.simmerly.recipes.data.mappers

import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.core.domain.dateStringToInstant
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NutritionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.SettingsEntity
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
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