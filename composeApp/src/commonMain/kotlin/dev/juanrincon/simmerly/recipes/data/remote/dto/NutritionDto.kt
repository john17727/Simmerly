package dev.juanrincon.simmerly.recipes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class NutritionDto(
    val calories: String?,
    val carbohydrateContent: String?,
    val cholesterolContent: String?,
    val fatContent: String?,
    val fiberContent: String?,
    val proteinContent: String?,
    val saturatedFatContent: String?,
    val sodiumContent: String?,
    val sugarContent: String?,
    val transFatContent: String?,
    val unsaturatedFatContent: String?
)
