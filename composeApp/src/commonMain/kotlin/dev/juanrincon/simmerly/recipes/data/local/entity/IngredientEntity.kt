package dev.juanrincon.simmerly.recipes.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ingredients")
data class IngredientEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    val quantity: Double,
    @ColumnInfo(name = "unit_id") val unitId: String?,
    @ColumnInfo(name = "food_id") val foodId: String?,
    val note: String,
    @ColumnInfo(name = "is_food") val isFood: Boolean,
    @ColumnInfo(name = "disable_amount") val disableAmount: Boolean,
    val display: String,
    val title: String?,
    @ColumnInfo(name = "original_text") val originalText: String?,
    @ColumnInfo(name = "reference_id") val referenceId: String
)
