package dev.juanrincon.simmerly.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.juanrincon.simmerly.recipes.data.local.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(entities = [RecipeEntity::class], version = 1)
abstract class SimmerlyDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao
}

@Suppress("KotlinNoActualForExpect")
expect object SimmerlyDatabaseConstructor : RoomDatabaseConstructor<SimmerlyDatabase> {
    override fun initialize(): SimmerlyDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<SimmerlyDatabase>
): SimmerlyDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}