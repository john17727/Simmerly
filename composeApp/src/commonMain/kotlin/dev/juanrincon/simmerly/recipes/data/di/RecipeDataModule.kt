package dev.juanrincon.simmerly.recipes.data.di

import dev.juanrincon.simmerly.core.di.AuthClient
import dev.juanrincon.simmerly.recipes.data.SimmerlyRecipeRepository
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import org.koin.dsl.module

val recipeDataModule = module {
    single<RecipeNetworkClient> { RecipeNetworkClient(get(qualifier = AuthClient)) }
    single<RecipeRepository> { SimmerlyRecipeRepository(get(), get()) }
}