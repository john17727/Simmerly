package dev.juanrincon.simmerly.core.di

import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.data.local.getRoomDatabase
import dev.juanrincon.simmerly.recipes.data.local.recipe.CategoryDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.FoodDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.IngredientDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.TagDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.ToolDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UnitDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserDao
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserRecipePreferenceDao
import org.koin.dsl.module

val localModule = module {
    single<SimmerlyDatabase> { getRoomDatabase(get()) }
    single<RecipeDao> { get<SimmerlyDatabase>().recipeDao() }
    single<IngredientDao> { get<SimmerlyDatabase>().ingredientDao() }
    single<UserDao> { get<SimmerlyDatabase>().userDao() }
    single<TagDao> { get<SimmerlyDatabase>().tagDao() }
    single<CategoryDao> { get<SimmerlyDatabase>().categoryDao() }
    single<ToolDao> { get<SimmerlyDatabase>().toolDao() }
    single<FoodDao> { get<SimmerlyDatabase>().foodDao() }
    single<UnitDao> { get<SimmerlyDatabase>().unitDao() }
    single<UserRecipePreferenceDao> { get<SimmerlyDatabase>().userRecipePreferenceDao() }
}