package dev.juanrincon.simmerly.core.di

import dev.juanrincon.simmerly.core.data.remote.createAuthenticatedHttpClient
import dev.juanrincon.simmerly.core.data.remote.createUnauthenticatedHttpClient
import dev.juanrincon.simmerly.core.data.remote.getPlatformEngine
import dev.juanrincon.simmerly.core.domain.LocalDataCleaner
import io.ktor.client.HttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val AuthClient = named("auth")
val PublicClient = named("public")

val remoteModule = module {
    single<HttpClient>(qualifier = AuthClient) {
        createAuthenticatedHttpClient(
            engine = getPlatformEngine(),
            sessionDatastore = get(),
            // Resolved on use: the cleaner depends on repositories that depend on this client.
            onSessionExpired = { get<LocalDataCleaner>().clearAll() },
        )
    }

    single<HttpClient>(qualifier = PublicClient) {
        createUnauthenticatedHttpClient(getPlatformEngine())
    }
}