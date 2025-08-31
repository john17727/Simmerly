package dev.juanrincon.simmerly.core.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<SimmerlyDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("simmerly.db")
    return Room.databaseBuilder<SimmerlyDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}