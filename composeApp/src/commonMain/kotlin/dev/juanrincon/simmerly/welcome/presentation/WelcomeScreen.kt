package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import org.jetbrains.compose.resources.painterResource
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.simmerly_logo
import simmerly.composeapp.generated.resources.welcome_background

@Composable
fun WelcomeScreen(
    state: WelcomeStore.State,
    onEvent: (WelcomeStore.Intent) -> Unit,
    windowSizeClass: WindowSizeClass
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.welcome_background),
            contentDescription = "Welcome background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(
                Color.Black.copy(alpha = 0.5f), // Example: 30% opaque black tint
                blendMode = BlendMode.Multiply
            )
        )
        if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Compact) {
            Column(
                modifier = Modifier.fillMaxSize().systemBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                LogoSection(modifier = Modifier.fillMaxWidth())
                LoginMobile(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
            }
        } else {
            Row(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                Column(
                    modifier = Modifier.fillMaxHeight().weight(2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    LogoSection(modifier = Modifier.weight(3f))
                }
                LoginDesktop(
                    state = state,
                    onEvent = onEvent,
                    modifier = Modifier.padding(16.dp).background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
                        .fillMaxHeight().weight(3f)
                )
            }
        }
    }
}

@Composable
fun LogoSection(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.simmerly_logo),
            contentDescription = "Simmerly Logo",
            modifier = Modifier.sizeIn(100.dp, 100.dp, 150.dp, 150.dp)
        )
        Text(
            text = "Welcome to Simmerly",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        )
        Text(
            text = "Your self‑hosted recipe nook.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun LoginMobile(
    state: WelcomeStore.State,
    onEvent: (WelcomeStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        OutlinedTextField(
            value = state.serverAddress,
            onValueChange = { onEvent(WelcomeStore.Intent.OnServerAddressChanged(it)) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            label = { Text("Server Address") })
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            WelcomeStore.LoginType.entries.forEach { type ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        type.ordinal,
                        WelcomeStore.LoginType.entries.size
                    ),
                    onClick = { onEvent(WelcomeStore.Intent.OnLoginTypeChanged(type)) },
                    selected = state.loginType == type,
                    icon = {},
                    label = { Text(type.displayName) },
                )
            }
        }
        when (state.loginType) {
            WelcomeStore.LoginType.CREDENTIALS -> {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { onEvent(WelcomeStore.Intent.OnUsernameChanged(it)) },
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    label = { Text("Username/Email") })
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onEvent(WelcomeStore.Intent.OnPasswordChanged(it)) },
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    label = { Text("Password") })
            }

            WelcomeStore.LoginType.API_KEY -> {
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = { onEvent(WelcomeStore.Intent.OnApiKeyChanged(it)) },
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    label = { Text("Api Key") })
            }
        }
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Text("Login")
        }
    }
}

@Composable
fun LoginDesktop(
    state: WelcomeStore.State,
    onEvent: (WelcomeStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        OutlinedTextField(
            value = state.serverAddress,
            onValueChange = { onEvent(WelcomeStore.Intent.OnServerAddressChanged(it)) },
            modifier = Modifier.fillMaxWidth(DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION).padding(16.dp),
            label = { Text("Server Address") })
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth(
                DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION
            ).padding(16.dp)
        ) {
            WelcomeStore.LoginType.entries.forEach { type ->
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(
                        type.ordinal,
                        WelcomeStore.LoginType.entries.size
                    ),
                    onClick = { onEvent(WelcomeStore.Intent.OnLoginTypeChanged(type)) },
                    selected = state.loginType == type,
                    icon = {},
                    label = { Text(type.displayName) },
                )
            }
        }
        when (state.loginType) {
            WelcomeStore.LoginType.CREDENTIALS -> {
                OutlinedTextField(
                    value = state.username,
                    onValueChange = { onEvent(WelcomeStore.Intent.OnUsernameChanged(it)) },
                    Modifier.fillMaxWidth(DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION)
                        .padding(horizontal = 16.dp),
                    label = { Text("Username/Email") })
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onEvent(WelcomeStore.Intent.OnPasswordChanged(it)) },
                    Modifier.fillMaxWidth(DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION)
                        .padding(horizontal = 16.dp),
                    label = { Text("Password") })
            }

            WelcomeStore.LoginType.API_KEY -> {
                OutlinedTextField(
                    value = state.apiKey,
                    onValueChange = { onEvent(WelcomeStore.Intent.OnApiKeyChanged(it)) },
                    Modifier.fillMaxWidth(DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION)
                        .padding(horizontal = 16.dp),
                    label = { Text("Api Key") })
            }
        }
        Button(
            onClick = {},
            modifier = Modifier.fillMaxWidth(DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION).padding(16.dp)
        ) {
            Text("Login")
        }
    }
}

const val DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION = 0.55f
