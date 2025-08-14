package dev.juanrincon.simmerly.core.di

import dev.juanrincon.simmerly.core.data.network.createHttpClient
import dev.juanrincon.simmerly.core.data.network.getPlatformEngine
import io.ktor.client.HttpClient
import org.koin.dsl.module

val coreModule = module {
    single<HttpClient> { createHttpClient(getPlatformEngine(), get()) }
}