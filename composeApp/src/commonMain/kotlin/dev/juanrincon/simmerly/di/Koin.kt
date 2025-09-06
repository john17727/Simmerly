package dev.juanrincon.simmerly.di

import dev.juanrincon.simmerly.auth.di.authModule
import dev.juanrincon.simmerly.core.di.localModule
import dev.juanrincon.simmerly.core.di.remoteModule
import dev.juanrincon.simmerly.recipes.data.di.recipeDataModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration

lateinit var simmerlyKoin: KoinApplication

fun initKoin(appDeclaration: KoinAppDeclaration = {}) {
    simmerlyKoin = startKoin {
        appDeclaration()
        modules(
            platformModule,
            remoteModule,
            localModule,
            authModule,
            recipeDataModule
        )
    }
}

fun initKoin() = initKoin {}