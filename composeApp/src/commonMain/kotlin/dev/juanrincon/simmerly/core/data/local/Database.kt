package dev.juanrincon.simmerly.core.data.local

import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.FoodEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.InstructionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NoteEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.ToolEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UnitEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeCategoryCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKey
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKeyDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.time.ExperimentalTime

@Database(
    entities = [
        RecipeEntity::class,
        CategoryEntity::class,
        CommentEntity::class,
        FoodEntity::class,
        IngredientEntity::class,
        InstructionEntity::class,
        NoteEntity::class,
        TagEntity::class,
        ToolEntity::class,
        UnitEntity::class,
        UserEntity::class,
        RecipeCategoryCrossRef::class,
        RecipeTagCrossRef::class,
        RecipeToolCrossRef::class,
        RecipeRemoteKey::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
@OptIn(ExperimentalTime::class)
@ConstructedBy(SimmerlyDatabaseConstructor::class)
abstract class SimmerlyDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    abstract fun recipeRemoteKeyDao(): RecipeRemoteKeyDao
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