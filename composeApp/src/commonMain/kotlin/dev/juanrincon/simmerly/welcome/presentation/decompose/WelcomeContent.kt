package dev.juanrincon.simmerly.welcome.presentation.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.welcome.presentation.WelcomeScreen

@Composable
fun WelcomeContent(welcomeComponent: WelcomeComponent, modifier: Modifier = Modifier) {
    val state by welcomeComponent.state.collectAsState()
    WelcomeScreen(state = state, onEvent = welcomeComponent::onEvent)
}