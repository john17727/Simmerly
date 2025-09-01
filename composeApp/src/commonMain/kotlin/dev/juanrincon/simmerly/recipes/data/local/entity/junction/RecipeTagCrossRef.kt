package dev.juanrincon.simmerly.recipes.data.local.entity.junction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.TagEntity

@Entity(
    tableName = "recipe_tag_cross_ref",
    primaryKeys = ["recipeId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"],
            onDelete = ForeignKey.CASCADE // If a recipe is deleted, delete its links
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE // If a tag is deleted, remove it from all recipes
        )
    ]
)
data class RecipeTagCrossRef(
    @ColumnInfo(name = "recipe_id", index = true) val recipeId: String,
    @ColumnInfo(name = "tag_id", index = true) val tagId: String
)
