package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.InstructionIngredientCrossRef

@Dao
interface InstructionIngredientDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(crossRefs: List<InstructionIngredientCrossRef>)

    @Query(
        "DELETE FROM instruction_ingredient_cross_ref WHERE instruction_id IN " +
                "(SELECT id FROM instructions WHERE recipe_id = :recipeId)"
    )
    suspend fun clearForRecipe(recipeId: String)
}
