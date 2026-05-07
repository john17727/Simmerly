package dev.juanrincon.simmerly.core.data.local

import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKey
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKeyDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.CommentDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.FoodDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.IngredientDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.InstructionDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.NoteDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeTagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeToolDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.TagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.ToolDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UnitDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserDao
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
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.InstructionIngredientCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeCategoryCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef
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
        InstructionIngredientCrossRef::class,
        RecipeRemoteKey::class
    ],
    version = 1
)
@TypeConverters(Converters::class)
@OptIn(ExperimentalTime::class)
@ConstructedBy(SimmerlyDatabaseConstructor::class)
abstract class SimmerlyDatabase : RoomDatabase() {
    abstract fun recipeDao(): RecipeDao

    abstract fun ingredientDao(): IngredientDao

    abstract fun unitDao(): UnitDao

    abstract fun foodDao(): FoodDao

    abstract fun instructionDao(): InstructionDao

    abstract fun toolDao(): ToolDao

    abstract fun tagDao(): TagDao

    abstract fun recipeToolDao(): RecipeToolDao

    abstract fun recipeTagDao(): RecipeTagDao

    abstract fun noteDao(): NoteDao

    abstract fun recipeRemoteKeyDao(): RecipeRemoteKeyDao

    abstract fun commentDao(): CommentDao

    abstract fun userDao(): UserDao
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