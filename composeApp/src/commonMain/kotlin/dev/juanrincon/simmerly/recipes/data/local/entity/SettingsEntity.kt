package dev.juanrincon.simmerly.recipes.data.local.entity

import androidx.room.ColumnInfo

data class SettingsEntity(
    val public: Boolean,
    @ColumnInfo(name = "show_nutrition") val showNutrition: Boolean,
    @ColumnInfo(name = "show_assets") val showAssets: Boolean,
    @ColumnInfo(name = "landscape_view") val landscapeView: Boolean,
    @ColumnInfo("disable_comments") val disableComments: Boolean,
    @ColumnInfo("disable_amount") val disableAmount: Boolean,
    val locked: Boolean
)
