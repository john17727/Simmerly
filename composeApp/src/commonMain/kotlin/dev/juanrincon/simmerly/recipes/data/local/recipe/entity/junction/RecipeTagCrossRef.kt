package dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.ForeignKey
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity

@Entity(
    tableName = "recipe_tag_cross_ref",
    primaryKeys = ["recipe_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE // If a recipe is deleted, delete its links
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE // If a tag is deleted, remove it from all recipes
        )
    ]
)
data class RecipeTagCrossRef(
    @ColumnInfo(name = "recipe_id", index = true) val recipeId: String,
    @ColumnInfo(name = "tag_id", index = true) val tagId: String
)
