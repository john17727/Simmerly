package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.FoodEntity

@Dao
interface FoodDao {

    @Upsert
    suspend fun upsertAll(foods: List<FoodEntity>)

    @Upsert
    suspend fun upsert(food: FoodEntity)

}