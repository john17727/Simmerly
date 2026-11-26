package dev.juanrincon.simmerly.di

import dev.juanrincon.simmerly.auth.di.authModule
import dev.juanrincon.simmerly.core.di.localModule
import dev.juanrincon.simmerly.core.di.remoteModule
import dev.juanrincon.simmerly.initialload.data.di.initialLoadDataModule
import dev.juanrincon.simmerly.initialload.presentation.di.initialLoadPresentationModule
import dev.juanrincon.simmerly.profile.di.profileModule
import dev.juanrincon.simmerly.recipes.data.di.recipeDataModule
import dev.juanrincon.simmerly.recipes.presentation.di.recipePresentationModule
import dev.juanrincon.simmerly.shoppinglists.data.di.shoppingListDataModule
import dev.juanrincon.simmerly.shoppinglists.presentation.di.shoppingListPresentationModule
import dev.juanrincon.simmerly.mealplan.data.di.mealPlanDataModule
import dev.juanrincon.simmerly.mealplan.presentation.di.mealPlanPresentationModule
import dev.juanrincon.simmerly.timeline.data.di.timelineDataModule
import dev.juanrincon.simmerly.timeline.presentation.di.timelinePresentationModule
import dev.juanrincon.simmerly.welcome.di.welcomeModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration

lateinit var simmerlyKoin: KoinApplication

/**
 * Every module the app runs with, so [initKoin] and the tests build the same graph. [platform]
 * can be swapped for a module backed by memory instead of the device's files.
 */
fun appModules(platform: Module = platformModule): List<Module> = listOf(
    appModule,
    platform,
    remoteModule,
    localModule,
    authModule,
    welcomeModule,
    recipeDataModule,
    recipePresentationModule,
    initialLoadDataModule,
    initialLoadPresentationModule,
    profileModule,
    shoppingListDataModule,
    shoppingListPresentationModule,
    timelineDataModule,
    mealPlanDataModule,
    mealPlanPresentationModule,
    timelinePresentationModule,
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    simmerlyKoin = startKoin {
        appDeclaration()
        modules(appModules())
    }
}

fun initKoin() = initKoin {}