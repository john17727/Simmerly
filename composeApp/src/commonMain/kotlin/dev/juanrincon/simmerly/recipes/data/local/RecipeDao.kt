package dev.juanrincon.simmerly.recipes.data.local

import androidx.room.Dao
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RecipeDao {

    fun getRecipes(): Flow<List<RecipeEntity>>
}