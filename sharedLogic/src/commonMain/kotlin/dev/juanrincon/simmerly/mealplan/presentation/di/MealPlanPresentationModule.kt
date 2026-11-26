package dev.juanrincon.simmerly.mealplan.presentation.di

import dev.juanrincon.simmerly.mealplan.presentation.MealPlanAddViewModel
import dev.juanrincon.simmerly.mealplan.presentation.MealPlanViewModel
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanAddArgs
import dev.juanrincon.simmerly.mealplan.presentation.MealPlanCartViewModel
import dev.juanrincon.simmerly.mealplan.presentation.orbit.MealPlanCartArgs
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val mealPlanPresentationModule = module {
    viewModel { MealPlanViewModel(get()) }
    viewModel { (args: MealPlanAddArgs) -> MealPlanAddViewModel(args, get(), get()) }
    viewModel { (args: MealPlanCartArgs) -> MealPlanCartViewModel(args, get(), get(), get(), get()) }
}
