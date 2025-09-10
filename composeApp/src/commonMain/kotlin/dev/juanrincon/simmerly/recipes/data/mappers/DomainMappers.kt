package dev.juanrincon.simmerly.recipes.data.mappers

import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

fun RecipeEntity.toDomain(host: String?): RecipeSummary = RecipeSummary(
    id = id,
    name = name,
    image = createRecipeImageUrl(host, id),
    tags = listOf(),
    rating = rating,
    cookTime = abbreviateTime(totalTime)
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