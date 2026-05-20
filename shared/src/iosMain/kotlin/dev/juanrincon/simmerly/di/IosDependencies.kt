package dev.juanrincon.simmerly.di

import dev.juanrincon.simmerly.auth.domain.AuthRepository

fun getAuthRepositoryFromKoin(): AuthRepository {
    return simmerlyKoin.koin.get()
}