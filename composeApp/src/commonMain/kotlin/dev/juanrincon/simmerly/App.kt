package dev.juanrincon.simmerly

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import dev.juanrincon.simmerly.navigation.app.AppContent
import dev.juanrincon.simmerly.navigation.auth.AuthDestinations
import dev.juanrincon.simmerly.navigation.auth.AuthNavigationViewModel
import dev.juanrincon.simmerly.splash.presentation.SplashScreen
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import dev.juanrincon.simmerly.welcome.presentation.WelcomeContent
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App() {
    SimmerlyTheme(dynamicColor = true) {
        SimmerlyApp(modifier = Modifier.fillMaxSize())
    }
}

@Composable
fun SimmerlyApp(
    viewModel: AuthNavigationViewModel = koinViewModel(),
    modifier: Modifier = Modifier
) {
    val backStack = rememberNavBackStack(
        configuration = SavedStateConfiguration {
            serializersModule = SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(AuthDestinations.Login::class, AuthDestinations.Login.serializer())
                    subclass(AuthDestinations.Splash::class, AuthDestinations.Splash.serializer())
                    subclass(AuthDestinations.App::class, AuthDestinations.App.serializer())
                }
            }
        },
        AuthDestinations.Splash
    )
    val authenticationState by viewModel.isAuthenticated.collectAsState()

    LaunchedEffect(authenticationState) {
        backStack.clear()
        backStack.add(authenticationState)
    }
    NavDisplay(
        backStack = backStack,
        modifier = modifier,
        entryProvider = { key ->
           when (key) {
              is AuthDestinations.Login -> {
                  NavEntry(key) {
                      WelcomeContent()
                  }
              }
               is AuthDestinations.Splash -> {
                   NavEntry(key) {
                       SplashScreen()
                   }
               }
               is AuthDestinations.App -> {
                   NavEntry(key) {
                       AppContent()
                   }
               }
               else -> error("Unknown key: $key")
           }
        }
    )
}
