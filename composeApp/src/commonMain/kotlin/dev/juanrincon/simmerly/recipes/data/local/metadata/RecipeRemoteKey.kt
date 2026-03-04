package dev.juanrincon.simmerly.recipes.data.local.metadata

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import kotlin.time.Instant

@Entity(
    tableName = "recipe_remote_keys",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipeId"]
        )
    ]
)
data class RecipeRemoteKey(
    @PrimaryKey(autoGenerate = false)
    val recipeId: String,
    val previousKey: Int?,
    val nextKey: Int?,
    val createdAt: Instant
)