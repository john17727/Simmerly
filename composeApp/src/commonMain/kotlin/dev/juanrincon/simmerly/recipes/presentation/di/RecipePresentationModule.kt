package dev.juanrincon.simmerly.recipes.presentation.di

import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsViewModel
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsViewModel
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListViewModel
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStoreFactory
import dev.juanrincon.simmerly.recipes.presentation.search.RecipeSearchViewModel
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recipePresentationModule = module {
    factoryOf(::RecipeListStoreFactory)
    viewModelOf(::RecipeListViewModel)
    viewModel { (recipeId: String) -> RecipeDetailsViewModel(recipeId, get(), get()) }
    viewModel { (recipeId: String) -> RecipeCommentsViewModel(recipeId, get(), get()) }
    factoryOf(::RecipeSearchStoreFactory)
    viewModelOf(::RecipeSearchViewModel)
}
