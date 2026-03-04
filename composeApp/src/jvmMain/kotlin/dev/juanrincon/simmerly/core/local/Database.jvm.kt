package dev.juanrincon.simmerly.core.local

import androidx.room.Room
import androidx.room.RoomDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import java.io.File

fun getDatabaseBuilder(): RoomDatabase.Builder<SimmerlyDatabase> {
    val dbFile = File(System.getProperty("java.io.tmpdir"), "simmerly.db")
    return Room.databaseBuilder<SimmerlyDatabase>(
        name = dbFile.absolutePath,
    )
}