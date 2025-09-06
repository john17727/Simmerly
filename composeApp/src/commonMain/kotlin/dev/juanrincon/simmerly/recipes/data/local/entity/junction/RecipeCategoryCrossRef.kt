package dev.juanrincon.simmerly.recipes.data.local.entity.junction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import dev.juanrincon.simmerly.recipes.data.local.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity

@Entity(
    tableName = "recipe_category_cross_ref",
    primaryKeys = ["recipe_id", "category_id"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE // If a recipe is deleted, delete its links
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["category_id"],
            onDelete = ForeignKey.CASCADE // If a category is deleted, remove it from all recipes
        )
    ]
)
data class RecipeCategoryCrossRef(
    @ColumnInfo(name = "recipe_id", index = true) val recipeId: String,
    @ColumnInfo(name = "category_id", index = true) val categoryId: String
)
