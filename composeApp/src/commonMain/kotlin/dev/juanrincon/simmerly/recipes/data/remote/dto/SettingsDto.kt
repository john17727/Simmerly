package dev.juanrincon.simmerly.recipes.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class SettingsDto(
    val public: Boolean,
    val showNutrition: Boolean,
    val showAssets: Boolean,
    val landscapeView: Boolean,
    val disableComments: Boolean,
    val locked: Boolean
)
