package dev.juanrincon.simmerly.mealplan.data.di

import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.core.di.AuthClient
import dev.juanrincon.simmerly.core.domain.ResettableCache
import dev.juanrincon.simmerly.mealplan.data.DefaultMealPlanRepository
import dev.juanrincon.simmerly.mealplan.data.HouseholdPreferencesStore
import dev.juanrincon.simmerly.mealplan.data.local.MealPlanEntryDao
import dev.juanrincon.simmerly.mealplan.data.remote.MealPlanNetworkClient
import dev.juanrincon.simmerly.mealplan.domain.MealPlanRepository
import org.koin.dsl.binds
import org.koin.dsl.module

val mealPlanDataModule = module {
    single { MealPlanNetworkClient(get(qualifier = AuthClient)) }
    single<MealPlanEntryDao> { get<SimmerlyDatabase>().mealPlanEntryDao() }
    single { HouseholdPreferencesStore(get()) }
    single { DefaultMealPlanRepository(get(), get(), get(), get(), get()) } binds arrayOf(
        MealPlanRepository::class,
        ResettableCache::class,
    )
}
