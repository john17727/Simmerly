package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef

@Dao
interface RecipeToolDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<RecipeToolCrossRef>)

    @Query("DELETE FROM recipe_tool_cross_ref WHERE recipe_id = :recipeId")
    suspend fun clearForRecipe(recipeId: String)
}