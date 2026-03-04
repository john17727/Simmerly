package dev.juanrincon.simmerly.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.tracktion.core.data.datastore.createDataStore
import app.tracktion.core.data.datastore.dataStoreFileName

fun createAndroidDataStore(context: Context): DataStore<Preferences> = createDataStore(
    producePath = { context.filesDir.resolve(dataStoreFileName).absolutePath }
)