package dev.juanrincon.simmerly.core.di

import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.data.local.getRoomDatabase
import dev.juanrincon.simmerly.recipes.data.local.recipe.RecipeDao
import org.koin.dsl.module

val localModule = module {
    single<SimmerlyDatabase> { getRoomDatabase(get()) }
    single<RecipeDao> { get<SimmerlyDatabase>().recipeDao() }
}