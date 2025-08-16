package dev.juanrincon.simmerly.core.data.network.utils.dynamic_base_url_ktor_plugin

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.concurrent.Volatile
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

class BaseUrlHolder(private val loadBaseUrls: suspend () -> String?) {

    @Volatile
    private var value: String? = null

    @Volatile
    private var isLoadRequest = false

    private val mutex = Mutex()

    suspend fun loadBaseUrl(): String? {
        if (value != null) return value

        val prevValue = value

        return if (coroutineContext[SetBaseUrlContext] != null) {
            value = loadBaseUrls()
            value
        } else {
            mutex.withLock {
                isLoadRequest = true
                try {
                    if (prevValue == value) {
                        value = loadBaseUrls()
                    }
                } finally {
                    isLoadRequest = false
                }
                value
            }
        }
    }

    private class SetBaseUrlContext : CoroutineContext.Element {
        override val key: CoroutineContext.Key<*>
            get() = SetBaseUrlContext

        companion object : CoroutineContext.Key<SetBaseUrlContext>
    }
}