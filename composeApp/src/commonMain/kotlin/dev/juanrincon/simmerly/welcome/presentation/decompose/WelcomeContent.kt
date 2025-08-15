package dev.juanrincon.simmerly.welcome.presentation.decompose

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.welcome.presentation.WelcomeScreen
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource

@Composable
fun WelcomeContent(
    welcomeComponent: WelcomeComponent,
    modifier: Modifier = Modifier,
    windowSizeClass: WindowSizeClass
) {
    val state by welcomeComponent.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe labels from the component's store
    LaunchedEffect(key1 = welcomeComponent.labels) { // Use store instance as key
        welcomeComponent.labels.collectLatest { label ->
            when (label) {
                is WelcomeStore.Label.LoginFailed -> { // Assuming this label exists
                    snackbarHostState.showSnackbar(
                        message = getString(label.message),
                    )
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        WelcomeScreen(
            state = state,
            onEvent = welcomeComponent::onEvent,
            windowSizeClass = windowSizeClass,
            // Pass innerPadding if WelcomeScreen is the direct child of Scaffold
            modifier = Modifier.fillMaxSize()
        )
    }
}