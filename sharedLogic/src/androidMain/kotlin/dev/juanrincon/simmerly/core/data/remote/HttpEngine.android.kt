package dev.juanrincon.simmerly.core.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.android.Android

actual fun getPlatformEngine(): HttpClientEngine = Android.create()