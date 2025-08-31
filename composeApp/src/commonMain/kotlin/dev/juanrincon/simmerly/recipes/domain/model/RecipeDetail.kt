package dev.juanrincon.simmerly.recipes.domain.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
data class RecipeDetail (
    val id: String,
    val userId: String,
    val householdId: String,
    val groupId: String,
    val name: String,
    val servings: Double,
    val recipeYieldQuantity: Double,
    val recipeYield: String,
    val totalTime: String,
    val prepTime: String?,
    val cookTime: String?,
    val performTime: String?,
    val description: String,
    val category: List<Category>,
    val tags: List<Tag>,
    val tools: List<Tool>,
    val rating: Double?,
    val originalUrl: String,
    val dateAdded: Instant,
    val dateUpdated: Instant,
    val createdAt: Instant,
    val updatedAt: Instant,
    val lastMade: Instant?,
    val ingredients: List<Ingredient>,
    val instructions: List<Instruction>,
    val nutrition: Nutrition,
    val settings: Settings,
    val assets: List<String>,
    val notes: List<Note>,
    val comments: List<Comment>
)
