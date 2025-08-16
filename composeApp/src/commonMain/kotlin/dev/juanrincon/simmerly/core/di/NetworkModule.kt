package dev.juanrincon.simmerly.core.di

import dev.juanrincon.simmerly.core.data.network.DataStoreBaseUrlProvider
import dev.juanrincon.simmerly.core.data.network.DataStoreTokenProvider
import dev.juanrincon.simmerly.core.data.network.createAuthenticatedHttpClient
import dev.juanrincon.simmerly.core.data.network.createUnauthenticatedHttpClient
import dev.juanrincon.simmerly.core.data.network.getPlatformEngine
import dev.juanrincon.simmerly.core.domain.network.BaseUrlProvider
import dev.juanrincon.simmerly.core.domain.network.TokenProvider
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.core.qualifier.named
import org.koin.dsl.module

val AuthClient = named("auth")
val PublicClient = named("public")

val networkModule = module {
    single<BaseUrlProvider> {
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
        DataStoreBaseUrlProvider(get(), scope)
    }
    single<TokenProvider> {
        DataStoreTokenProvider(get())
    }

    single<HttpClient>(qualifier = AuthClient) {
        createAuthenticatedHttpClient(getPlatformEngine(), get(), get())
    }

    single<HttpClient>(qualifier = PublicClient) {
        createUnauthenticatedHttpClient(getPlatformEngine(), get())
    }
}