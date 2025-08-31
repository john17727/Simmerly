package dev.juanrincon.simmerly.recipes.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
@Entity(tableName = "units")
data class UnitEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "plural_name") val pluralName: String,
    val description: String,
    val fraction: Boolean,
    val abbreviation: String,
    @ColumnInfo(name = "plural_abbreviation") val pluralAbbreviation: String?,
    @ColumnInfo(name = "use_abbreviation") val useAbbreviation: Boolean,
    @ColumnInfo(name = "created_at") val createdAt: Instant,
    @ColumnInfo(name = "updated_at") val updatedAt: Instant
)
