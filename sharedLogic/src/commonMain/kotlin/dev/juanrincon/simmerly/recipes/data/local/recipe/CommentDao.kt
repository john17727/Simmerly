package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.CommentWithRelations
import kotlinx.coroutines.flow.Flow

@Dao
interface CommentDao {

    @Upsert
    suspend fun upsertAll(comments: List<CommentEntity>)

    @Upsert
    suspend fun upsert(comment: CommentEntity)

    @Transaction
    @Query("SELECT * FROM comments WHERE recipe_id = :recipeId ORDER BY created_at ASC")
    fun observeComments(recipeId: String): Flow<List<CommentWithRelations>>

    @Query("DELETE FROM comments WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)

}