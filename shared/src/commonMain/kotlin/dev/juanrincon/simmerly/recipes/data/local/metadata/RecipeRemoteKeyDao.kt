package dev.juanrincon.simmerly.recipes.data.local.metadata

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert

@Dao
interface RecipeRemoteKeyDao {

    @Upsert
    suspend fun upsert(remoteKey: RecipeRemoteKey)

    @Query("SELECT * FROM recipe_remote_keys WHERE id = 'RECIPE_LIST' LIMIT 1")
    suspend fun getKey(): RecipeRemoteKey?

    @Query("DELETE FROM recipe_remote_keys")
    suspend fun clearKey()
}
