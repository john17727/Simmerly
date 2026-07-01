package dev.juanrincon.simmerly.core.data.local

import androidx.room3.ColumnTypeConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class Converters {
    @ColumnTypeConverter
    fun fromInstant(instant: Instant?): String? {
        // Converts our Instant into a String that SQLite can store
        return instant?.toString()
    }

    @ColumnTypeConverter
    fun toInstant(value: String?): Instant? {
        // Converts the String from the database back into our Instant
        return value?.let { Instant.parse(it) }
    }

    @ColumnTypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(",") // Simple, but brittle. Using JSON is better.
    }

    @ColumnTypeConverter
    fun toStringList(data: String): List<String> {
        return data.split(",")
    }
}