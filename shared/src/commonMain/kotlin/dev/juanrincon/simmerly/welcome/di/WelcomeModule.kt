package dev.juanrincon.simmerly.welcome.di

import dev.juanrincon.simmerly.welcome.presentation.WelcomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val welcomeModule = module {
    viewModelOf(::WelcomeViewModel)
}