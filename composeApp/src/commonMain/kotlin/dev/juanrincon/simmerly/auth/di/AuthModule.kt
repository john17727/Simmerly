package dev.juanrincon.simmerly.auth.di

import dev.juanrincon.simmerly.auth.data.DataStoreSessionStorage
import dev.juanrincon.simmerly.auth.data.DefaultAuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.SessionStorage
import org.koin.dsl.module

val authModule = module {
    single<AuthRepository> { DefaultAuthRepository(get()) }
    single<SessionStorage> { DataStoreSessionStorage(get()) }
}