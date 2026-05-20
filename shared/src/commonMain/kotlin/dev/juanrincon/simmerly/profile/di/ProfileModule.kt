package dev.juanrincon.simmerly.profile.di

import dev.juanrincon.simmerly.profile.data.DefaultProfileRepository
import dev.juanrincon.simmerly.profile.domain.ProfileRepository
import dev.juanrincon.simmerly.profile.presentation.ProfileViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val profileModule = module {
    single<ProfileRepository> { DefaultProfileRepository(get(), get()) }
    viewModelOf(::ProfileViewModel)
}
