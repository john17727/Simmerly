package dev.juanrincon.simmerly.core.data.local

import androidx.room3.TypeConverter
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class Converters {
    @TypeConverter
    fun fromInstant(instant: Instant?): String? {
        // Converts our Instant into a String that SQLite can store
        return instant?.toString()
    }

    @TypeConverter
    fun toInstant(value: String?): Instant? {
        // Converts the String from the database back into our Instant
        return value?.let { Instant.parse(it) }
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(",") // Simple, but brittle. Using JSON is better.
    }

    @TypeConverter
    fun toStringList(data: String): List<String> {
        return data.split(",")
    }
}