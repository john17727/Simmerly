package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Transaction
import androidx.room.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity

@Dao
interface IngredientDao {

    @Upsert
    suspend fun upsertAll(ingredients: List<IngredientEntity>)

    @Upsert
    suspend fun upsert(ingredient: IngredientEntity)
}