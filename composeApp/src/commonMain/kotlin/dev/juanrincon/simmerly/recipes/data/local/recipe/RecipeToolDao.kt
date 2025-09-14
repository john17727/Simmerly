package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef

@Dao
interface RecipeToolDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<RecipeToolCrossRef>)

    @Query("DELETE FROM recipe_tool_cross_ref WHERE recipe_id = :recipeId")
    suspend fun clearForRecipe(recipeId: String)
}