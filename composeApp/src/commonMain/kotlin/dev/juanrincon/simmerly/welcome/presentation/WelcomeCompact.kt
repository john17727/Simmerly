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
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.theme.SimmerlyTheme
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
            WelcomeCollage(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.weight(1f))
            Header(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.weight(1f))
            Login(state, onEvent, modifier = Modifier.fillMaxWidth().padding(16.dp))
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.padding(innerPadding.calculateBottomPadding()))
        }
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicPreview() {
    SimmerlyTheme(dynamicColor = true) {
        CompactWelcome(
            state = WelcomeStore.State(),
            onEvent = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun LightPreview() {
    SimmerlyTheme {
        CompactWelcome(state = WelcomeStore.State(), onEvent = {})
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicDarkPreview() {
    SimmerlyTheme(dynamicColor = true, darkTheme = true) {
        CompactWelcome(state = WelcomeStore.State(), onEvent = {})
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun DarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        CompactWelcome(state = WelcomeStore.State(), onEvent = {})
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun LoadingPreview() {
    SimmerlyTheme {
        CompactWelcome(state = WelcomeStore.State(isLoading = true), onEvent = {})
    }
}
