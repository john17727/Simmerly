package dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing

import dev.juanrincon.simmerly.recipes.data.remote.dto.AssetDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.CategoryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.CommentDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.IngredientDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.InstructionDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.NoteDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.NutritionDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.SettingsDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.TagDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.ToolDto
import kotlinx.serialization.Serializable

@Serializable
data class RecipePatchDto(
    val id: String? = null,
    val userId: String? = null,
    val householdId: String? = null,
    val groupId: String? = null,
    val name: String? = null,
    val slug: String? = null,
    val image: String? = null,
    val recipeServings: Double? = null,
    val recipeYieldQuantity: Double? = null,
    val recipeYield: String? = null,
    val totalTime: String? = null,
    val prepTime: String? = null,
    val cookTime: String? = null,
    val performTime: String? = null,
    val description: String? = null,
    val recipeCategory: List<CategoryDto>? = null,
    val tags: List<TagDto>? = null,
    val tools: List<ToolDto>? = null,
    val rating: Double? = null,
    val orgURL: String? = null,
    val dateAdded: String? = null,
    val dateUpdated: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val lastMade: String? = null,
    val recipeIngredient: List<IngredientDto>? = null,
    val recipeInstructions: List<InstructionDto>? = null,
    val nutrition: NutritionDto? = null,
    val settings: SettingsDto? = null,
    val assets: List<AssetDto>? = null,
    val notes: List<NoteDto>? = null,
    val comments: List<CommentDto>? = null
)
