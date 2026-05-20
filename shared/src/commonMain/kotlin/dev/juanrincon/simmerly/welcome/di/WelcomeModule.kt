package dev.juanrincon.simmerly.welcome.di

import dev.juanrincon.simmerly.welcome.presentation.WelcomeViewModel
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreFactory
import org.koin.core.module.dsl.factoryOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val welcomeModule = module {
    viewModelOf(::WelcomeViewModel)
    factoryOf(::WelcomeStoreFactory)
}