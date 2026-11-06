package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeCategoryCrossRef

@Dao
interface CategoryDao {
    @Upsert
    suspend fun upsertAll(categories: List<CategoryEntity>)

    @Upsert
    suspend fun upsert(category: CategoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<RecipeCategoryCrossRef>)
}
