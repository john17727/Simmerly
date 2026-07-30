package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.InstructionEntity

@Dao
interface InstructionDao {

    @Upsert
    suspend fun upsertAll(instructions: List<InstructionEntity>)

    @Upsert
    suspend fun upsert(instruction: InstructionEntity)

    @Query("DELETE FROM instructions WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)
}