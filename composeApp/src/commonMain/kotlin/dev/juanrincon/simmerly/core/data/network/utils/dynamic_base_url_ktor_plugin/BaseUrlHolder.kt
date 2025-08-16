package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class BaseUrlHolder(private val loadBaseUrls: suspend () -> String?) {

    @Volatile
    private var value: String? = null

    private val mutex = Mutex()

    suspend fun loadBaseUrl(): String? {
        if (value != null) return value

        return mutex.withLock {
            value ?: loadBaseUrls()?.also { loaded -> value = loaded }
        }
    }
}