package dev.juanrincon.simmerly.welcome.presentation.decompose

import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface WelcomeComponent {
    val state: StateFlow<WelcomeStore.State>
    val labels: Flow<WelcomeStore.Label>

    fun onEvent(event: WelcomeStore.Intent)
}