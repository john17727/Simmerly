package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UnitEntity

@Dao
interface UnitDao {

    @Upsert
    suspend fun upsertAll(units: List<UnitEntity>)

    @Upsert
    suspend fun upsert(unit: UnitEntity)
}