package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
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
}