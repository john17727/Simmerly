package dev.juanrincon.simmerly.recipes.domain.model

data class Settings(
    val public: Boolean,
    val showNutrition: Boolean,
    val showAssets: Boolean,
    val landscapeView: Boolean,
    val disableComments: Boolean,
    val disableAmount: Boolean,
    val locked: Boolean
)
