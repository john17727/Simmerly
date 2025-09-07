package dev.juanrincon.simmerly.recipes.data.mappers

import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

fun RecipeEntity.toDomain(): RecipeSummary = RecipeSummary(
    id = id,
    name = name,
    image = image,
    tags = listOf(),
    rating = rating
)