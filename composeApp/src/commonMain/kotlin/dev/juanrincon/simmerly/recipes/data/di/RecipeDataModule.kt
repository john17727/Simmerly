package dev.juanrincon.simmerly.recipes.data.di

import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.recipes.data.SimmerlyRecipeRepository
import dev.juanrincon.simmerly.recipes.data.local.RecipeDao
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import org.koin.dsl.module

val recipeDataModule = module {
    single<RecipeNetworkClient> { RecipeNetworkClient(get()) }
    single<RecipeDao> { get<SimmerlyDatabase>().recipeDao() }
    single<RecipeRepository> { SimmerlyRecipeRepository(get(), get()) }
}