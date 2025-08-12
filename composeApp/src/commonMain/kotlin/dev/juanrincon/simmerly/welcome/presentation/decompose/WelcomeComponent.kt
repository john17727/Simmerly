package dev.juanrincon.simmerly.welcome.presentation.decompose

import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import kotlinx.coroutines.flow.StateFlow

interface WelcomeComponent {
    val state: StateFlow<WelcomeStore.State>

    fun onEvent(event: WelcomeStore.Intent)
}