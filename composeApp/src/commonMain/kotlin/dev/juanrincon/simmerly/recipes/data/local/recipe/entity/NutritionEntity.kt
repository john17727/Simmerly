package dev.juanrincon.simmerly.recipes.data.local.recipe.entity

import androidx.room.ColumnInfo

data class NutritionEntity(
    val calories: String?,
    val carbohydrates: String?,
    val cholesterol: String?,
    val fat: String?,
    val fiber: String?,
    val protein: String?,
    @ColumnInfo(name = "saturated_fat") val saturatedFat: String?,
    val sodium: String?,
    val sugar: String?,
    @ColumnInfo(name = "trans_fat") val transFat: String?,
    @ColumnInfo(name = "unsaturated_fat") val unsaturatedFat: String?
) {
    companion object {
        fun empty() = NutritionEntity(
            calories = null,
            carbohydrates = null,
            cholesterol = null,
            fat = null,
            fiber = null,
            protein = null,
            saturatedFat = null,
            sodium = null,
            sugar = null,
            transFat = null,
            unsaturatedFat = null
        )
    }
}
