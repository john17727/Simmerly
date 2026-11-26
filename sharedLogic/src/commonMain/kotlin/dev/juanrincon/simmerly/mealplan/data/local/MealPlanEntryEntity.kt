package dev.juanrincon.simmerly.mealplan.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey

/**
 * Dates are ISO strings (sort and compare correctly in SQL). Recipe columns come from the embedded
 * `recipe` of the server's entry so the plan renders offline. No foreign key to `recipes`: a list
 * refresh clears that table.
 */
@Entity(tableName = "meal_plan_entries", indices = [Index("date"), Index("recipe_id")])
data class MealPlanEntryEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "group_id") val groupId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val date: String,
    @ColumnInfo(name = "end_date") val endDate: String?,
    @ColumnInfo(name = "entry_type") val entryType: String,
    val title: String,
    val text: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String?,
    @ColumnInfo(name = "recipe_scale") val recipeScale: Double,
    @ColumnInfo(name = "recipe_name") val recipeName: String?,
    @ColumnInfo(name = "recipe_servings") val recipeServings: Double?,
)
