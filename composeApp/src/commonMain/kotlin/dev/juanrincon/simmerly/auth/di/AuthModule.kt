package dev.juanrincon.simmerly.auth.di

import dev.juanrincon.simmerly.auth.data.DefaultAuthRepository
import dev.juanrincon.simmerly.auth.data.DefaultSessionDataStore
import dev.juanrincon.simmerly.auth.data.network.AuthNetworkClient
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.core.di.PublicClient
import dev.juanrincon.simmerly.navigation.auth.AuthViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val authModule = module {
    single<AuthNetworkClient> { AuthNetworkClient(get(qualifier = PublicClient)) }
    single<AuthRepository> { DefaultAuthRepository(get(), get()) }
    single<SessionDataStore> { DefaultSessionDataStore(get()) }
    viewModelOf(::AuthViewModel)
}