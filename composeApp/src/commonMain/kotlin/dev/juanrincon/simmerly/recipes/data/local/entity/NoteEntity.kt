package dev.juanrincon.simmerly.recipes.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey val id: String,
    val title: String,
    val text: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String
)