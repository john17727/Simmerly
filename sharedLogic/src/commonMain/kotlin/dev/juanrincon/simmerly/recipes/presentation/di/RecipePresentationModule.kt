package dev.juanrincon.simmerly.recipes.presentation.di

import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsViewModel
import dev.juanrincon.simmerly.recipes.presentation.cookmode.CookModeViewModel
import dev.juanrincon.simmerly.recipes.presentation.cookmode.CookTimerAlerts
import dev.juanrincon.simmerly.recipes.presentation.cookmode.defaultCookTimerAlerts
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsViewModel
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListViewModel
import dev.juanrincon.simmerly.recipes.presentation.search.RecipeSearchViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val recipePresentationModule = module {
    viewModelOf(::RecipeListViewModel)
    viewModel { (recipeId: String) -> RecipeDetailsViewModel(recipeId, get()) }
    viewModel { (recipeId: String) -> RecipeCommentsViewModel(recipeId, get()) }
    viewModel { (recipeId: String) -> CookModeViewModel(recipeId, get(), get()) }
    viewModelOf(::RecipeSearchViewModel)

    single<CookTimerAlerts> { defaultCookTimerAlerts() }
}
