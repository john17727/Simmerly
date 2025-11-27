package dev.juanrincon.simmerly.welcome.presentation

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.welcome.presentation.decompose.WelcomeComponent
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class WelcomeViewModel(
    welcomeStoreFactory: WelcomeStoreFactory,
) : ViewModel(), WelcomeComponent {
    private val store = welcomeStoreFactory.create()

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<WelcomeStore.State> = store.stateFlow

    override val labels: Flow<WelcomeStore.Label> = store.labels

    override fun onEvent(event: WelcomeStore.Intent) = store.accept(event)
}