package dev.juanrincon.simmerly.recipes.data.local.metadata

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlin.time.Instant

@Dao
interface RecipeRemoteKeyDao {

    @Upsert
    suspend fun upsertAll(remoteKeys: List<RecipeRemoteKey>)

    @Query("SELECT * FROM recipe_remote_keys WHERE recipeId = :id")
    suspend fun getRemoteKeyByRecipeId(id: String): RecipeRemoteKey?

    @Query("SELECT createdAt FROM recipe_remote_keys ORDER BY createdAt DESC LIMIT 1")
    suspend fun getCreationTime(): Instant?

    @Query("DELETE FROM recipe_remote_keys")
    suspend fun clearRemoteKeys()
}