package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserRecipePreferenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserRecipePreferenceDao {
    @Upsert
    suspend fun upsertAll(preferences: List<UserRecipePreferenceEntity>)

    @Query("SELECT * FROM user_recipe_preferences WHERE recipeId = :recipeId")
    fun observe(recipeId: String): Flow<UserRecipePreferenceEntity?>

    @Query("SELECT * FROM user_recipe_preferences")
    fun observeAll(): Flow<List<UserRecipePreferenceEntity>>

    @Query("SELECT * FROM user_recipe_preferences")
    suspend fun getAll(): List<UserRecipePreferenceEntity>
}
