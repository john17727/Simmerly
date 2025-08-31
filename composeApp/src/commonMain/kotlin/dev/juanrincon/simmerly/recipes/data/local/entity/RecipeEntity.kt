package dev.juanrincon.simmerly.recipes.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RecipeEntity(
    @PrimaryKey val id: String,
)
