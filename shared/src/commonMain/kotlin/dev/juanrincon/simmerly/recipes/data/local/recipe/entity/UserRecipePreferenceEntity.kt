package dev.juanrincon.simmerly.recipes.data.local.recipe.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "user_recipe_preferences")
data class UserRecipePreferenceEntity(
    @PrimaryKey val recipeId: String,
    val rating: Double?,
    val isFavorite: Boolean,
)
