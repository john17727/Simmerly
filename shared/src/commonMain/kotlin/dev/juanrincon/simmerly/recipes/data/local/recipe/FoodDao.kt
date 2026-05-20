package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.FoodEntity

@Dao
interface FoodDao {

    @Upsert
    suspend fun upsertAll(foods: List<FoodEntity>)

    @Upsert
    suspend fun upsert(food: FoodEntity)

}