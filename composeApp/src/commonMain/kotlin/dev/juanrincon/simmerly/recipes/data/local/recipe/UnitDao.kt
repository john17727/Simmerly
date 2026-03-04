package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UnitEntity

@Dao
interface UnitDao {

    @Upsert
    suspend fun upsertAll(units: List<UnitEntity>)

    @Upsert
    suspend fun upsert(unit: UnitEntity)
}