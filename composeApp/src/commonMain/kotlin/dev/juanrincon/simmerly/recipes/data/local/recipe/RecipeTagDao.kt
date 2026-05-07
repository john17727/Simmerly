package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef

@Dao
interface RecipeTagDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(crossRefs: List<RecipeTagCrossRef>)

    @Query("DELETE FROM recipe_tag_cross_ref WHERE recipe_id = :recipeId")
    suspend fun clearForRecipe(recipeId: String)
}