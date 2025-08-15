package dev.juanrincon.simmerly.navigation.auth

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.popTo
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.juanrincon.simmerly.auth.domain.AuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
import dev.juanrincon.simmerly.navigation.app.DefaultAppComponent
import dev.juanrincon.simmerly.splash.presentation.decompose.DefaultSplashComponent
import dev.juanrincon.simmerly.welcome.presentation.decompose.DefaultWelcomeComponent
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent

class DefaultRootComponent(
    componentContext: ComponentContext,
    val storeFactory: StoreFactory,
    private val authRepository: AuthRepository
) : RootComponent,
    ComponentContext by componentContext,
    KoinComponent {

    private val navigation = StackNavigation<AuthConfiguration>()

    override val stack: Value<ChildStack<*, RootComponent.Child>> = childStack(
        source = navigation,
        serializer = AuthConfiguration.serializer(),
        initialConfiguration = AuthConfiguration.Splash,
        handleBackButton = true,
        childFactory = ::createChild
    )

    init {
        authRepository.observeAuthState().onEach { authState ->
            val currentConfig = stack.value.active.configuration
            when (authState) {
                is AuthState.Authenticated -> {
                    // Check if already on main to avoid redundant navigation
                    if (currentConfig !is AuthConfiguration.App) { // Example config
                        navigation.replaceAll(AuthConfiguration.App)
                    }
                }

                is AuthState.Unauthenticated -> {
                    navigation.replaceAll(AuthConfiguration.Welcome) // Or Login directly
                }

                is AuthState.Loading -> {
                    navigation.replaceAll(AuthConfiguration.Splash)
                }
            }
        }.launchIn(coroutineScope())
    }

    override fun onBackClicked(toIndex: Int) {
        navigation.popTo(toIndex)
    }

    private fun createChild(
        config: AuthConfiguration,
        componentContext: ComponentContext
    ): RootComponent.Child =
        when (config) {
            AuthConfiguration.Welcome -> RootComponent.Child.WelcomeChild(
                DefaultWelcomeComponent(
                    componentContext,
                    storeFactory,
                    authRepository
                )
            )

            AuthConfiguration.Splash -> RootComponent.Child.SplashChild(
                DefaultSplashComponent(
                    componentContext
                )
            )

            AuthConfiguration.App -> RootComponent.Child.AppChild(
                DefaultAppComponent(
                    componentContext
                )
            )
        }

    @Serializable
    sealed interface AuthConfiguration {
        @Serializable
        data object Welcome : AuthConfiguration

        @Serializable
        data object Splash : AuthConfiguration

        @Serializable
        data object App : AuthConfiguration
    }
}