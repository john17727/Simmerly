package dev.juanrincon.simmerly.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.datastore.createDesktopDataStore
import dev.juanrincon.simmerly.core.local.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DataStore<Preferences>> { createDesktopDataStore() }
    single<RoomDatabase.Builder<SimmerlyDatabase>> { getDatabaseBuilder() }
}