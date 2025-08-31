package dev.juanrincon.simmerly.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.juanrincon.simmerly.recipes.data.local.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.FoodEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.InstructionEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.NoteEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.ToolEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.UnitEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.UserEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO

@Database(
    entities = [RecipeEntity::class, CategoryEntity::class, CommentEntity::class, FoodEntity::class, IngredientEntity::class, InstructionEntity::class, NoteEntity::class, TagEntity::class, ToolEntity::class, UnitEntity::class, UserEntity::class],
    version = 1
)
@TypeConverters(Converters::class)
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