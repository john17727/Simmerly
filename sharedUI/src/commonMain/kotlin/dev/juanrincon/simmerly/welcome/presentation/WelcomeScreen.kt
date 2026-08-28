package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextObfuscationMode
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedSecureTextField
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.ToggleButtonDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.window.core.layout.WindowSizeClass
import dev.juanrincon.simmerly.theme.Simmerly
import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeIntent
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeState

@Composable
fun WelcomeScreen(
    state: WelcomeState,
    onEvent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSizeClass = currentWindowAdaptiveInfoV2().windowSizeClass
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            keyboardController?.hide()
        }
    }
    if (windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)) {
        ExpandedWelcome(state, onEvent, modifier = modifier)
    } else {
        CompactWelcome(state, onEvent, modifier = modifier)
    }
}

@Composable
fun Header(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Simmerly",
            style = MaterialTheme.typography.displayMedium.copy(letterSpacing = (-0.5).sp),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "Your self‑hosted recipe nook.",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 18.sp,
                lineHeight = 26.sp
            ),
            textAlign = TextAlign.Center,
            // The design's #4C8C63 only reads on white; dark theme needs the light
            // sage. Derived from the scheme so it also tracks an explicit darkTheme
            // override and dynamic color, which isSystemInDarkTheme() would miss.
            color = if (MaterialTheme.colorScheme.surface.luminance() > 0.5f) {
                Simmerly.HerbSage500
            } else {
                MaterialTheme.colorScheme.tertiary
            }
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Login(
    state: WelcomeState,
    onEvent: (WelcomeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    var passwordVisible by remember { mutableStateOf(false) }
    val serverAddressFieldState = rememberTextFieldState()
    val usernameFieldState = rememberTextFieldState()
    val passwordFieldState = rememberTextFieldState()

    LaunchedEffect(serverAddressFieldState) {
        snapshotFlow { serverAddressFieldState.text.toString() }
            .collect { onEvent(WelcomeIntent.OnServerAddressChanged(it)) }
    }
    LaunchedEffect(usernameFieldState) {
        snapshotFlow { usernameFieldState.text.toString() }
            .collect { onEvent(WelcomeIntent.OnUsernameChanged(it)) }
    }
    LaunchedEffect(passwordFieldState) {
        snapshotFlow { passwordFieldState.text.toString() }
            .collect { onEvent(WelcomeIntent.OnPasswordChanged(it)) }
    }

    Column(modifier = modifier) {
        OutlinedTextField(
            state = serverAddressFieldState,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Server Address") },
            placeholder = { Text("https://mealie.home.lan") },
            leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Uri
            ),
        )
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            ToggleButton(
                checked = state.credentialType == CredentialType.CREDENTIALS,
                onCheckedChange = { onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.CREDENTIALS)) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                enabled = !state.isLoading,
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text("Credentials")
            }
            ToggleButton(
                checked = state.credentialType == CredentialType.API_TOKEN,
                onCheckedChange = { onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN)) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                enabled = !state.isLoading,
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            ) {
                Icon(Icons.Default.Key, contentDescription = null)
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text("API Token")
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
        AnimatedContent(
            targetState = state.credentialType,
            label = "CredentialFields",
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { credType ->
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (credType == CredentialType.CREDENTIALS) {
                    OutlinedTextField(
                        state = usernameFieldState,
                        label = { Text("Username/Email") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        ),
                    )
                }
                OutlinedSecureTextField(
                    state = passwordFieldState,
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        if (credType == CredentialType.CREDENTIALS) Text("Password") else Text("Token")
                    },
                    leadingIcon = {
                        if (credType == CredentialType.CREDENTIALS) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        } else {
                            Icon(Icons.Default.Key, contentDescription = null)
                        }
                    },
                    onKeyboardAction = { onEvent(WelcomeIntent.OnLoginClicked) },
                    enabled = !state.isLoading,
                    textObfuscationMode = if (passwordVisible) TextObfuscationMode.Visible else TextObfuscationMode.RevealLastTyped,
                    trailingIcon = {
                        val image =
                            if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        val description = if (passwordVisible) "Hide password" else "Show password"
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, description)
                        }
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(28.dp))
        Button(
            onClick = { onEvent(WelcomeIntent.OnLoginClicked) },
            enabled = state.isLoginButtonEnabled,
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().height(56.dp),
        ) {
            AnimatedContent(
                targetState = state.isLoading,
                label = "LoginButtonContent"
            ) { isLoading ->
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .sizeIn(maxHeight = 24.dp, maxWidth = 24.dp)
                            .testTag("login_loading_indicator")
                    )
                } else {
                    Text(
                        text = "Login",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.1.sp
                        )
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(20.dp))
    }
}
