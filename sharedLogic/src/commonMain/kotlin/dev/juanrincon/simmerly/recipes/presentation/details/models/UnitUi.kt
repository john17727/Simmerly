package dev.juanrincon.simmerly.recipes.presentation.details.models

data class UnitUi(
    val name: String,
    val pluralName: String?,
    val fraction: Boolean,
    val abbreviation: String,
    val pluralAbbreviation: String?,
    val useAbbreviation: Boolean,
)
