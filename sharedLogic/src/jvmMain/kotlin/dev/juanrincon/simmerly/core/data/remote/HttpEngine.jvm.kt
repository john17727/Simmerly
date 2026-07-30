package dev.juanrincon.simmerly.core.data.remote

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java

actual fun getPlatformEngine(): HttpClientEngine = Java.create()