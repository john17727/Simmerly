package dev.juanrincon.simmerly.recipes.data.network.dtos

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDto(
    val id: String,
    val userId: String,
    val householdId: String,
    val groupId: String,
    val name: String,
    val slug: String,
    val image: String,
    val recipeServings: Double,
    val recipeYieldQuantity: Double,
    val recipeYield: String,
    val totalTime: String,
    val prepTime: String? = null,
    val cookTime: String? = null,
    val performTime: String? = null,
    val description: String,
    val recipeCategory: List<CategoryDto>,
    val tags: List<TagDto>,
    val tools: List<ToolDto>,
    val rating: Double? = null,
    val orgURL: String,
    val dateAdded: String,
    val dateUpdated: String,
    val createdAt: String,
    val updatedAt: String,
    val lastMade: String? = null
)