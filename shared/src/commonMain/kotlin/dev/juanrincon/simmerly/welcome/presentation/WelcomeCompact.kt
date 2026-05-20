package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore

@Composable
internal fun CompactWelcome(
    state: WelcomeStore.State,
    onEvent: (WelcomeStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold { innerPadding ->
        Column(
            modifier = modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WelcomeCollage(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp).graphicsLayer()
            )
            Header(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.weight(1f))
            Login(state, onEvent, modifier = Modifier.fillMaxWidth().padding(16.dp))
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(innerPadding.calculateBottomPadding()))
        }
    }
}
