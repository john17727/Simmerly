package dev.juanrincon.simmerly.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import dev.juanrincon.simmerly.datastore.createDesktopDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DataStore<Preferences>> { createDesktopDataStore() }
}