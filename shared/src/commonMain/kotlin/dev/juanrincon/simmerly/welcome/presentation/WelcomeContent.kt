package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.getString
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun WelcomeContent(
    viewModel: WelcomeViewModel = koinViewModel(),
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Observe labels from the component's store
    LaunchedEffect(key1 = viewModel.labels) { // Use store instance as key
        viewModel.labels.collectLatest { label ->
            when (label) {
                is WelcomeStore.Label.LoginFailed -> { // Assuming this label exists
                    snackbarHostState.showSnackbar(
                        message = getString(label.message),
                    )
                }
            }
        }
    }
    WelcomeScreen(
        state = state,
        onEvent = viewModel::onEvent,
        // Pass innerPadding if WelcomeScreen is the direct child of Scaffold
        modifier = Modifier.fillMaxSize()
    )
}