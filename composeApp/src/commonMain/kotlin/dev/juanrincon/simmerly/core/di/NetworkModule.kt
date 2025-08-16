package dev.juanrincon.simmerly.core.di

import dev.juanrincon.simmerly.core.data.network.createAuthenticatedHttpClient
import dev.juanrincon.simmerly.core.data.network.createUnauthenticatedHttpClient
import dev.juanrincon.simmerly.core.data.network.getPlatformEngine
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val AuthClient = named("auth")
val PublicClient = named("public")

val networkModule = module {
    single<HttpClient>(qualifier = AuthClient) {
        createAuthenticatedHttpClient(getPlatformEngine(), get())
    }

    single<HttpClient>(qualifier = PublicClient) {
        createUnauthenticatedHttpClient(getPlatformEngine())
    }
}