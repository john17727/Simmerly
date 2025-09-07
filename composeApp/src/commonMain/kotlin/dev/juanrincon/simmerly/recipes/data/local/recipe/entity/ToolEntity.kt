package dev.juanrincon.simmerly.recipes.data.local.recipe.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tools")
data class ToolEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "group_id") val groupId: String,
    val name: String,
    val slug: String
)
