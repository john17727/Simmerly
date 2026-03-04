package dev.juanrincon.simmerly.recipes.data.local.recipe.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "ingredients",
    foreignKeys = [
        ForeignKey(
            entity = RecipeEntity::class,
            parentColumns = ["id"],
            childColumns = ["recipe_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["id"],
            childColumns = ["unit_id"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = FoodEntity::class,
            parentColumns = ["id"],
            childColumns = ["food_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class IngredientEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id", index = true) val recipeId: String,
    val quantity: Double?,
    @ColumnInfo(name = "unit_id", index = true) val unitId: String?,
    @ColumnInfo(name = "food_id", index = true) val foodId: String?,
    val note: String?,
    val display: String,
    val title: String?,
    @ColumnInfo(name = "original_text") val originalText: String?,
)
