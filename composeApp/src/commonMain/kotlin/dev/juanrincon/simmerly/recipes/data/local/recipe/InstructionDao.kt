package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
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