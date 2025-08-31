package dev.juanrincon.simmerly.recipes.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class FoodDto(
    val id: String,
    val name: String,
    val pluralName: String,
    val description: String,
    val labelId: String,
//    val aliases: List<String>, // Assuming this is a list of strings TODO: Check if this is correct
//    val householdsWithIngredientFood: List<String>, // Assuming this is a list of strings TODO: Check if this is correct
    val label: LabelDto,
    val createdAt: String,
    val updatedAt: String
)
