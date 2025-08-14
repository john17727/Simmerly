package dev.juanrincon.simmerly.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.tracktion.core.data.datastore.createAndroidDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<DataStore<Preferences>> { createAndroidDataStore(get()) }
}