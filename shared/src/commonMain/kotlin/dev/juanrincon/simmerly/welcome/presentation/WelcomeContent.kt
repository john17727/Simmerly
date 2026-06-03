package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeSideEffect
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun WelcomeContent(
    viewModel: WelcomeViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    viewModel.collectSideEffect { sideEffect ->
        when (sideEffect) {
            is WelcomeSideEffect.LoginFailed ->
                snackbarHostState.showSnackbar(message = getString(sideEffect.message))
        }
    }

    WelcomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        // Pass innerPadding if WelcomeScreen is the direct child of Scaffold
        modifier = Modifier.fillMaxSize()
    )
}