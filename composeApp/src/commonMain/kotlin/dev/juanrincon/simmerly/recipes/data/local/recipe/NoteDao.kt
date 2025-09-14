package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NoteEntity

@Dao
interface NoteDao {

    @Upsert
    suspend fun upsertAll(notes: List<NoteEntity>)

    @Upsert
    suspend fun upsert(note: NoteEntity)

    @Query("Delete FROM notes WHERE recipe_id = :recipeId")
    suspend fun deleteByRecipeId(recipeId: String)

}