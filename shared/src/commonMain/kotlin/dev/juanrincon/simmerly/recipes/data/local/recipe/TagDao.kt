package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity

@Dao
interface TagDao {
    @Upsert
    suspend fun upsertAll(tags: List<TagEntity>)

    @Upsert
    suspend fun upsert(tag: TagEntity)
}