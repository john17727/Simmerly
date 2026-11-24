package dev.juanrincon.simmerly.core.data.local

import androidx.room3.AutoMigration
import androidx.room3.ColumnTypeConverters
import androidx.room3.DeleteColumn
import androidx.room3.ConstructedBy
import androidx.room3.Database
import androidx.room3.RoomDatabase
import androidx.room3.RoomDatabaseConstructor
import androidx.room3.migration.AutoMigrationSpec
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKey
import dev.juanrincon.simmerly.recipes.data.local.metadata.RecipeRemoteKeyDao
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentSearchQueryDao
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentSearchQueryEntity
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentlyViewedDao
import dev.juanrincon.simmerly.recipes.data.local.recent.RecentlyViewedEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.CategoryDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.CommentDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.FoodDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.IngredientDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.InstructionDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.InstructionIngredientDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.NoteDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeTagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeToolDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.TagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.ToolDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UnitDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserRecipePreferenceDao
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
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserRecipePreferenceEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.InstructionIngredientCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeCategoryCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListDao
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListEntity
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListItemEntity
import dev.juanrincon.simmerly.shoppinglists.data.local.ShoppingListRecipeEntity
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventDao
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventEntity
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
        RecipeRemoteKey::class,
        RecentlyViewedEntity::class,
        RecentSearchQueryEntity::class,
        UserRecipePreferenceEntity::class,
        CacheMetadataEntity::class,
        ShoppingListEntity::class,
        ShoppingListItemEntity::class,
        ShoppingListRecipeEntity::class,
        TimelineEventEntity::class,
    ],
    version = 10,
    // Older versions still fall back to a destructive migration (see getRoomDatabase).
    autoMigrations = [
        AutoMigration(from = 5, to = 6),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8, spec = RemoteKeyCreatedAtDeleted::class),
        AutoMigration(from = 8, to = 9, spec = RecipeCookTimeDeleted::class),
        AutoMigration(from = 9, to = 10),
    ],
)
@ColumnTypeConverters(Converters::class)
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

    abstract fun instructionIngredientDao(): InstructionIngredientDao

    abstract fun noteDao(): NoteDao

    abstract fun recipeRemoteKeyDao(): RecipeRemoteKeyDao

    abstract fun commentDao(): CommentDao

    abstract fun userDao(): UserDao

    abstract fun categoryDao(): CategoryDao

    abstract fun userRecipePreferenceDao(): UserRecipePreferenceDao

    abstract fun recentlyViewedDao(): RecentlyViewedDao

    abstract fun recentSearchQueryDao(): RecentSearchQueryDao

    abstract fun cacheMetadataDao(): CacheMetadataDao

    abstract fun shoppingListDao(): ShoppingListDao

    abstract fun timelineEventDao(): TimelineEventDao
}

@Suppress("KotlinNoActualForExpect")
expect object SimmerlyDatabaseConstructor : RoomDatabaseConstructor<SimmerlyDatabase> {
    override fun initialize(): SimmerlyDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<SimmerlyDatabase>
): SimmerlyDatabase {
    return builder
        .fallbackToDestructiveMigration(true)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}

/** 7 -> 8: the cursor row's `createdAt` was written but never read, so the column is dropped. */
@DeleteColumn(tableName = "recipe_remote_keys", columnName = "createdAt")
class RemoteKeyCreatedAtDeleted : AutoMigrationSpec

/** 8 -> 9: Mealie 3.31 dropped `cookTime` from the recipe API (values moved to `performTime`). */
@DeleteColumn(tableName = "recipes", columnName = "cook_time")
class RecipeCookTimeDeleted : AutoMigrationSpec
