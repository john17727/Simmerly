package dev.juanrincon.simmerly.recipes.data.local.recipe.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "instructions")
data class InstructionEntity(
    @PrimaryKey  val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    val title: String,
    val summary: String,
    val text: String,
)
