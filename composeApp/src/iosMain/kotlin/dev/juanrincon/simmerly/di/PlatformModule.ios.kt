package dev.juanrincon.simmerly.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room.RoomDatabase
import app.tracktion.core.data.datastore.createNativeDataStore
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.data.local.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DataStore<Preferences>> { createNativeDataStore() }
    single<RoomDatabase.Builder<SimmerlyDatabase>> { getDatabaseBuilder() }
}
