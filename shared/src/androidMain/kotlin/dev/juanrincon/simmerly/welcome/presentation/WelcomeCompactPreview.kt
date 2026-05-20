package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import org.jetbrains.compose.resources.PreviewContextConfigurationEffect

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicPreview() {
    PreviewContextConfigurationEffect()
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
    PreviewContextConfigurationEffect()
    SimmerlyTheme {
        CompactWelcome(state = WelcomeStore.State(), onEvent = {})
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO, wallpaper = 1)
@Composable
private fun DynamicDarkPreview() {
    PreviewContextConfigurationEffect()
    SimmerlyTheme(dynamicColor = true, darkTheme = true) {
        CompactWelcome(state = WelcomeStore.State(), onEvent = {})
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun DarkPreview() {
    PreviewContextConfigurationEffect()
    SimmerlyTheme(darkTheme = true) {
        CompactWelcome(state = WelcomeStore.State(), onEvent = {})
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun LoadingPreview() {
    PreviewContextConfigurationEffect()
    SimmerlyTheme {
        CompactWelcome(state = WelcomeStore.State(isLoading = true), onEvent = {})
    }
}
