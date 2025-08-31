package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class RecipeDetailDto(
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
    val prepTime: String?,
    val cookTime: String?,
    val performTime: String?,
    val description: String,
    val recipeCategory: List<CategoryDto>,
    val tags: List<TagDto>,
    val tools: List<ToolDto>,
    val rating: Double?,
    val orgURL: String,
    val dateAdded: String,
    val dateUpdated: String,
    val createdAt: String,
    val updatedAt: String,
    val lastMade: String?,
    val recipeIngredient: List<IngredientDto>,
    val recipeInstructions: List<InstructionDto>,
    val nutrition: NutritionDto,
    val settings: SettingsDto,
    val assets: List<AssetDto>,
    val notes: List<NoteDto>,
    val comments: List<CommentDto>
)
