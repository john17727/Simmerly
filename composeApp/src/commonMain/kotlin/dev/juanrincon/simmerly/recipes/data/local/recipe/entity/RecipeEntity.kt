package dev.juanrincon.simmerly.recipes.data.local.recipe.entity

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Entity(tableName = "recipes")
data class RecipeEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "user_id") val userId: String,
    @ColumnInfo(name = "household_id") val householdId: String,
    @ColumnInfo(name = "group_id") val groupId: String,
    val name: String,
    val slug: String,
    val image: String,
    val servings: Double,
    @ColumnInfo(name = "yield_quantity") val yieldQuantity: Double,
    val yield: String,
    @ColumnInfo(name = "total_time") val totalTime: String,
    @ColumnInfo(name = "prep_time") val prepTime: String?,
    @ColumnInfo(name = "cook_time") val cookTime: String?,
    @ColumnInfo(name = "perform_time") val performTime: String?,
    val description: String,
    val rating: Double?,
    @ColumnInfo(name = "original_url") val originalUrl: String,
    @ColumnInfo(name = "date_added") val dateAdded: Instant,
    @ColumnInfo(name = "date_updated") val dateUpdated: Instant,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant,
    @ColumnInfo(name = "last_made") val lastMade: Instant?,
    @Embedded(prefix = "nutrition_")
    val nutrition: NutritionEntity,
    @Embedded(prefix = "settings_")
    val settings: SettingsEntity
)
