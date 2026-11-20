package dev.juanrincon.simmerly.recipes.data.di

import dev.juanrincon.simmerly.core.di.AuthClient
import dev.juanrincon.simmerly.recipes.data.SimmerlyRecipeRepository
import dev.juanrincon.simmerly.recipes.data.remote.RecipeNetworkClient
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.core.domain.ResettableCache
import org.koin.dsl.binds
import org.koin.dsl.module

val recipeDataModule = module {
    single<RecipeNetworkClient> { RecipeNetworkClient(get(qualifier = AuthClient)) }
    single { SimmerlyRecipeRepository(get(), get(), get(), get(), get()) } binds arrayOf(
        RecipeRepository::class,
        ResettableCache::class,
    )
}