package dev.juanrincon.simmerly.mealplan.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import androidx.room3.ColumnInfo
import kotlinx.coroutines.flow.Flow

data class RecipeDateRow(@ColumnInfo(name = "recipe_id") val recipeId: String, val date: String)

@Dao
interface MealPlanEntryDao {

    @Upsert
    suspend fun upsertAll(entries: List<MealPlanEntryEntity>)

    /** Entries touching [start]..[end]: single-day ones inside it, week ones overlapping it. */
    @Query(
        "SELECT * FROM meal_plan_entries WHERE date <= :end AND COALESCE(end_date, date) >= :start " +
            "ORDER BY date, id"
    )
    fun observeRange(start: String, end: String): Flow<List<MealPlanEntryEntity>>

    @Query(
        "SELECT COUNT(*) FROM meal_plan_entries WHERE date <= :end AND COALESCE(end_date, date) >= :start"
    )
    suspend fun countRange(start: String, end: String): Int

    /** Drops cached rows in the range the server no longer returned. */
    @Query(
        "DELETE FROM meal_plan_entries WHERE date <= :end AND COALESCE(end_date, date) >= :start " +
            "AND id NOT IN (:keepIds)"
    )
    suspend fun deleteMissingInRange(start: String, end: String, keepIds: List<Int>)

    @Query("DELETE FROM meal_plan_entries WHERE date <= :end AND COALESCE(end_date, date) >= :start")
    suspend fun deleteRange(start: String, end: String)

    @Query("DELETE FROM meal_plan_entries WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM meal_plan_entries WHERE id = :id")
    suspend fun getById(id: Int): MealPlanEntryEntity?

    @Query("SELECT recipe_id, date FROM meal_plan_entries WHERE recipe_id IS NOT NULL AND end_date IS NULL ORDER BY date")
    fun observeRecipeDates(): Flow<List<RecipeDateRow>>
}
