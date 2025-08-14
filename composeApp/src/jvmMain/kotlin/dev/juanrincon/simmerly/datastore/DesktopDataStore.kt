package dev.juanrincon.simmerly.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.tracktion.core.data.datastore.createDataStore
import app.tracktion.core.data.datastore.dataStoreFileName
import java.io.File

fun createDesktopDataStore(): DataStore<Preferences> = createDataStore(
    producePath = {
        val file = File(System.getProperty("java.io.tmpdir"), dataStoreFileName)
        file.absolutePath
    }
)