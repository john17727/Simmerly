package dev.juanrincon.simmerly.core.data.network

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.java.Java

actual fun getPlatformEngine(): HttpClientEngine = Java.create()