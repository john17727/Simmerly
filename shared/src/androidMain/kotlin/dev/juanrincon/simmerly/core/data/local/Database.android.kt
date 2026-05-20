package dev.juanrincon.simmerly.core.data.local

import android.content.Context
import androidx.room3.Room
import androidx.room3.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<SimmerlyDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("simmerly.db")
    return Room.databaseBuilder<SimmerlyDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}