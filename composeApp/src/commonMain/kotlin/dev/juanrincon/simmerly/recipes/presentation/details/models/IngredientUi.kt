package dev.juanrincon.simmerly.recipes.presentation.details.models

import dev.juanrincon.simmerly.core.utils.capitalizeWords
import dev.juanrincon.simmerly.core.utils.format
import dev.juanrincon.simmerly.core.utils.nullIfEmpty

data class IngredientUi(
    val quantity: Double,
    val display: String,
    val isFood: Boolean,
    val food: FoodUi?,
    val unit: UnitUi?,
    val note: String?
) {
    val formattedDisplay: String = if (isFood) {
        if (quantity > 1.0) {
            (food?.pluralName ?: food?.name ?: "").capitalizeWords()
        } else {
            (food?.name ?: "").capitalizeWords()
        }
    } else {
        display
    }

    val formattedQuantity = if (isFood) {
        val name = if (unit?.useAbbreviation == true) {
            unit.abbreviation
        } else {
            unit?.name ?: ""
        }
        val pluralName = if (unit?.useAbbreviation == true) {
            unit.pluralAbbreviation
        } else {
            unit?.pluralName ?: ""
        }
        val formattedQuantity = quantity.format(1)
        val formattedUnit = if (quantity > 1.0) {
            (pluralName ?: name).nullIfEmpty()
        } else {
            name.nullIfEmpty()
        }
        if (formattedUnit == null) {
            formattedQuantity
        } else {
            "$formattedQuantity $formattedUnit"
        }
    } else {
        null
    }
}
