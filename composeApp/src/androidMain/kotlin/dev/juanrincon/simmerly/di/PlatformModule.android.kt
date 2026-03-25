package dev.juanrincon.simmerly.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.room3.RoomDatabase
import dev.juanrincon.simmerly.core.data.datastore.createAndroidDataStore
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.data.local.getDatabaseBuilder
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DataStore<Preferences>> { createAndroidDataStore(get()) }
    single<RoomDatabase.Builder<SimmerlyDatabase>> { getDatabaseBuilder(get()) }
}