package dev.juanrincon.simmerly.shoppinglists.data.remote.dto

import dev.juanrincon.simmerly.recipes.data.remote.dto.LabelDto
import kotlinx.serialization.Serializable

/** Mealie's `ParsedIngredient`: free text split into quantity, unit, food and note. */
@Serializable
data class ParsedIngredientDto(
    val ingredient: ParsedRecipeIngredientDto,
)

@Serializable
data class ParsedRecipeIngredientDto(
    val quantity: Double? = null,
    val unit: ParsedUnitDto? = null,
    val food: ParsedFoodDto? = null,
    val note: String? = null,
)

/** A unit the parser recognised; [id] is only set when it matched one that already exists. */
@Serializable
data class ParsedUnitDto(
    val id: String? = null,
    val name: String,
    val pluralName: String? = null,
    val abbreviation: String = "",
    val useAbbreviation: Boolean = false,
)

/** A food the parser recognised; [id] is only set when it matched one that already exists. */
@Serializable
data class ParsedFoodDto(
    val id: String? = null,
    val name: String,
    val pluralName: String? = null,
    val labelId: String? = null,
    val label: LabelDto? = null,
)
