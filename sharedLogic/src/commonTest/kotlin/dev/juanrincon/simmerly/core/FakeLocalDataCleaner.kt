package dev.juanrincon.simmerly.core

import dev.juanrincon.simmerly.core.domain.LocalDataCleaner

class FakeLocalDataCleaner : LocalDataCleaner {
    var clearCount = 0
        private set

    override suspend fun clearAll() {
        clearCount++
    }
}
