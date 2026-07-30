package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.ToolEntity

@Dao
interface ToolDao {
    @Upsert
    suspend fun upsertAll(tools: List<ToolEntity>)

    @Upsert
    suspend fun upsert(tool: ToolEntity)
}