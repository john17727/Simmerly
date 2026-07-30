package dev.juanrincon.simmerly.core

import androidx.room3.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase

fun buildTestDatabase(): SimmerlyDatabase =
    Room.inMemoryDatabaseBuilder<SimmerlyDatabase>()
        .setDriver(BundledSQLiteDriver())
        .build()
