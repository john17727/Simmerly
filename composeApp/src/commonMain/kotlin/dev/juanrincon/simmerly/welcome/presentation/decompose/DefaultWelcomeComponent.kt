package dev.juanrincon.simmerly.welcome.presentation.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class DefaultWelcomeComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) : WelcomeComponent, ComponentContext by componentContext {
    private val store = instanceKeeper.getStore { WelcomeStoreProvider(storeFactory, authRepository).provide() }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<WelcomeStore.State> = store.stateFlow

    override fun onEvent(event: WelcomeStore.Intent) = store.accept(event)
}