package dev.juanrincon.simmerly.initialload.presentation.di

import dev.juanrincon.simmerly.initialload.presentation.InitialLoadViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val initialLoadPresentationModule = module {
    viewModelOf(::InitialLoadViewModel)
}
