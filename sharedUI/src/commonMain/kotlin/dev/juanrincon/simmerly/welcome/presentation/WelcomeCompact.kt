package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeIntent
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeState

@Composable
internal fun CompactWelcome(
    state: WelcomeState,
    onEvent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { innerPadding ->
        // Scrolls once the keyboard leaves too little room, while still filling the screen
        // (so the weighted spacers keep centering the form) when it doesn't.
        BoxWithConstraints(modifier = modifier.fillMaxSize().imePadding()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = maxHeight),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                WelcomeCollageCompact(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).graphicsLayer()
                )
                Header(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Login(state, onEvent, modifier = Modifier.fillMaxWidth().padding(16.dp))
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.padding(innerPadding.calculateBottomPadding()))
            }
        }
    }
}
