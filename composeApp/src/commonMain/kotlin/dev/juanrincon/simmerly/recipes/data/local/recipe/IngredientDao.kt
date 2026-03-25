package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity

@Dao
interface IngredientDao {

    @Upsert
    suspend fun upsertAll(ingredients: List<IngredientEntity>)

    @Upsert
    suspend fun upsert(ingredient: IngredientEntity)

    @Query("DELETE FROM ingredients WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)

}