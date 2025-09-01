package dev.juanrincon.simmerly.recipes.data.local.entity.junction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.ToolEntity

@Entity(
    tableName = "recipe_tool_cross_ref",
    primaryKeys = ["recipeId", "toolId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE // If a recipe is deleted, delete its links
        ),
        ForeignKey(
            entity = ToolEntity::class,
            parentColumns = ["id"],
            childColumns = ["toolId"],
            onDelete = ForeignKey.CASCADE // If a tool is deleted, remove it from all recipes
        )
    ]
)
data class RecipeToolCrossRef(
    @ColumnInfo(name = "recipe_id", index = true) val recipeId: String,
    @ColumnInfo(name = "tool_id", index = true) val toolId: String
)
