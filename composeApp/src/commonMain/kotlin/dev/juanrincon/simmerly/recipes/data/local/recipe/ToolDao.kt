package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.ToolEntity

@Dao
interface ToolDao {
    @Upsert
    suspend fun upsertAll(tools: List<ToolEntity>)

    @Upsert
    suspend fun upsert(tool: ToolEntity)
}