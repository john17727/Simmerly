package dev.juanrincon.simmerly.recipes.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "instructions")
data class InstructionEntity(
    @PrimaryKey  val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    val title: String,
    val summary: String,
    val text: String
)
