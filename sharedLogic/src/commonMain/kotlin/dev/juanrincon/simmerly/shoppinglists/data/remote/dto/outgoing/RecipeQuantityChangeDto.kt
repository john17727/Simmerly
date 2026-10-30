package dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

@Serializable
data class RecipeIncrementDto(
    val recipeIncrementQuantity: Double,
    /** Only these of the recipe's ingredients are added; left out (the default isn't encoded) for all. */
    val recipeIngredients: List<RecipeIngredientDto>? = null,
)

/** A recipe ingredient as Mealie expects it back; its food and unit need only their ids and names. */
@Serializable
data class RecipeIngredientDto(
    val quantity: Double?,
    val unit: RecipeIngredientUnitDto?,
    val food: RecipeIngredientFoodDto?,
    val note: String?,
    val display: String,
    val title: String?,
    val originalText: String?,
    val referenceId: String,
)

@Serializable
data class RecipeIngredientFoodDto(
    val id: String,
    val name: String,
    val pluralName: String?,
    val labelId: String?,
)

@Serializable
data class RecipeIngredientUnitDto(
    val id: String,
    val name: String,
    val pluralName: String?,
    val abbreviation: String,
    val pluralAbbreviation: String?,
    val useAbbreviation: Boolean,
    val fraction: Boolean,
)

@Serializable
data class RecipeDecrementDto(val recipeDecrementQuantity: Double)
