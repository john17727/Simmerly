package dev.juanrincon.simmerly.di

import dev.juanrincon.simmerly.auth.di.authModule
import dev.juanrincon.simmerly.core.di.localModule
import dev.juanrincon.simmerly.core.di.remoteModule
import dev.juanrincon.simmerly.initialload.data.di.initialLoadDataModule
import dev.juanrincon.simmerly.initialload.presentation.di.initialLoadPresentationModule
import dev.juanrincon.simmerly.recipes.data.di.recipeDataModule
import dev.juanrincon.simmerly.recipes.presentation.di.recipePresentationModule
import dev.juanrincon.simmerly.welcome.di.welcomeModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

lateinit var simmerlyKoin: KoinApplication

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    simmerlyKoin = startKoin {
        appDeclaration()
        modules(
            appModule,
            platformModule,
            remoteModule,
            localModule,
            authModule,
            welcomeModule,
            recipeDataModule,
            recipePresentationModule,
            initialLoadDataModule,
            initialLoadPresentationModule
        )
    }
}

fun initKoin() = initKoin {}