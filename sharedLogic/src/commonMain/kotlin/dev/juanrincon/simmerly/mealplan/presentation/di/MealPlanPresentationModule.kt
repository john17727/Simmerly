package dev.juanrincon.simmerly.mealplan.presentation.di

import dev.juanrincon.simmerly.mealplan.presentation.MealPlanAddViewModel
import dev.juanrincon.simmerly.mealplan.presentation.MealPlanViewModel
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mealPlanPresentationModule = module {
    viewModel { MealPlanViewModel(get()) }
    viewModel { (args: MealPlanAddArgs) -> MealPlanAddViewModel(args, get(), get()) }
}
