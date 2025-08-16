package dev.juanrincon.simmerly.core.data.network

import dev.juanrincon.simmerly.auth.domain.SessionStorage
import dev.juanrincon.simmerly.core.domain.network.BaseUrlProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.concurrent.Volatile

class DataStoreBaseUrlProvider(
    sessionStorage: SessionStorage,
    scope: CoroutineScope,
) : BaseUrlProvider {
    @Volatile
    private var latest: String? = null

    init {
        // Keep a hot, cached copy for fast synchronous access
        scope.launch {
            sessionStorage.observeServerAddress().collect { latest = it }
        }
    }

    override fun current(): String? = latest
}