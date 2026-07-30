package dev.juanrincon.simmerly.initialload.presentation.di

import dev.juanrincon.simmerly.initialload.presentation.InitialLoadViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val initialLoadPresentationModule = module {
    viewModelOf(::InitialLoadViewModel)
}
