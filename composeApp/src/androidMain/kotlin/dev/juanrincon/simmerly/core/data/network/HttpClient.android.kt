package dev.juanrincon.simmerly.core.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.engine.okhttp.OkHttpEngine

actual fun getPlatformEngine(): HttpClientEngine = OkHttp.create()