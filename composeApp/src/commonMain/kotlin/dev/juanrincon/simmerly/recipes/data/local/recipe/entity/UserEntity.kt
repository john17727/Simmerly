package dev.juanrincon.simmerly.recipes.data.local.recipe.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val username: String,
    val admin: Boolean,
    @ColumnInfo(name = "full_name") val fullName: String
)
