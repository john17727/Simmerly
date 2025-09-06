package dev.juanrincon.simmerly.recipes.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.model.RecipeDetailWithRelations
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Dao
interface RecipeDao {

    /**
     * Provides a PagingSource for the Paging 3 library to read from the database.
     * The list is ordered by creation date to ensure a consistent UI.
     */
    @Query("SELECT * FROM recipes ORDER BY created_at DESC limit :limit offset :offset")
    fun getAll(limit: Int, offset: Int): List<RecipeEntity>

    /**
     * Observes a single recipe with all its relations (tags, ingredients, etc.).
     * The @Transaction annotation is crucial to ensure atomic reads across multiple tables.
     * Returns a Flow to make the UI reactive to any database changes.
     */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipeDetail(id: String): Flow<RecipeDetailWithRelations>

    /**
     * Inserts or updates a list of recipes.
     * This is the primary function for saving data fetched from the network.
     */
    @Upsert
    suspend fun upsertAll(recipes: List<RecipeEntity>)

    /**
     * Deletes a specific recipe.
     */
    @Delete
    suspend fun delete(recipe: RecipeEntity)

    /**
     * Deletes all recipes from the table.
     * Essential for the RemoteMediator's refresh logic.
     */
    @Query("DELETE FROM recipes")
    suspend fun clearAll()
}