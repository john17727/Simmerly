package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.RecipeDetailWithRelations
import kotlinx.coroutines.flow.Flow
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Dao
interface RecipeDao {

    /**
     * Provides a PagingSource for the Paging 3 library to read from the database.
     * The list is ordered by creation date to ensure a consistent UI.
     */
    @Query("SELECT * FROM recipes ORDER BY created_at DESC")
    fun observeRecipeList(): Flow<List<RecipeEntity>>

    /**
     * Observes a single recipe with all its relations (tags, ingredients, etc.).
     * The @Transaction annotation is crucial to ensure atomic reads across multiple tables.
     * Returns a Flow to make the UI reactive to any database changes.
     */
    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    fun observeRecipeDetail(id: String): Flow<RecipeDetailWithRelations>

    @Transaction
    @Query("SELECT * FROM recipes WHERE id = :id")
    suspend fun getRecipeDetail(id: String): RecipeDetailWithRelations

    @Upsert
    suspend fun upsert(recipe: RecipeEntity)

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