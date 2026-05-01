package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import org.jetbrains.compose.resources.painterResource
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.simmerly_logo

@Composable
fun WelcomeScreen(
    state: WelcomeStore.State,
    onEvent: (WelcomeStore.Intent) -> Unit,
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
fun Logo(modifier: Modifier = Modifier) {
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
fun Header(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Welcome to Simmerly",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Text(
            text = "Your self‑hosted recipe nook.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.tertiary,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun Login(
    state: WelcomeStore.State,
    onEvent: (WelcomeStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    var passwordVisible by remember { mutableStateOf(false) }
    Column(modifier = modifier) {
        OutlinedTextField(
            value = state.serverAddress,
            onValueChange = { onEvent(WelcomeStore.Intent.OnServerAddressChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Server Address") },
            leadingIcon = { Icon(Icons.Default.Dns, contentDescription = null) },
            enabled = !state.isLoading,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Next,
                keyboardType = KeyboardType.Text
            ),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
        )
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween)
        ) {
            ToggleButton(
                checked = state.credentialType == WelcomeStore.CredentialType.CREDENTIALS,
                onCheckedChange = { onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.CREDENTIALS)) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                enabled = !state.isLoading,
                shapes = ButtonGroupDefaults.connectedLeadingButtonShapes(),
            ) {
                Icon(Icons.Default.Person, contentDescription = null)
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text("Credentials")
            }
            ToggleButton(
                checked = state.credentialType == WelcomeStore.CredentialType.API_TOKEN,
                onCheckedChange = { onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN)) },
                modifier = Modifier.weight(1f).semantics { role = Role.RadioButton },
                enabled = !state.isLoading,
                shapes = ButtonGroupDefaults.connectedTrailingButtonShapes(),
            ) {
                Icon(Icons.Default.Key, contentDescription = null)
                Spacer(Modifier.size(ToggleButtonDefaults.IconSpacing))
                Text("API Token")
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        AnimatedContent(
            targetState = state.credentialType,
            label = "CredentialFields",
            transitionSpec = { fadeIn() togetherWith fadeOut() }
        ) { credType ->
            Column {
                if (credType == WelcomeStore.CredentialType.CREDENTIALS) {
                    OutlinedTextField(
                        value = state.username,
                        onValueChange = { onEvent(WelcomeStore.Intent.OnUsernameChanged(it)) },
                        label = { Text("Username/Email") },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        enabled = !state.isLoading,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Email
                        ),
                        keyboardActions = KeyboardActions(onNext = {
                            focusManager.moveFocus(
                                FocusDirection.Down
                            )
                        })
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedTextField(
                    value = state.password,
                    onValueChange = { onEvent(WelcomeStore.Intent.OnPasswordChanged(it)) },
                    modifier = Modifier.fillMaxWidth(),
                    label = {
                        if (credType == WelcomeStore.CredentialType.CREDENTIALS) Text("Password") else Text(
                            "Token"
                        )
                    },
                    leadingIcon = {
                        if (credType == WelcomeStore.CredentialType.CREDENTIALS) {
                            Icon(Icons.Default.Lock, contentDescription = null)
                        } else {
                            Icon(Icons.Default.Key, contentDescription = null)
                        }
                    },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions.Default.copy(
                        imeAction = ImeAction.Done,
                        keyboardType = KeyboardType.Password
                    ),
                    keyboardActions = KeyboardActions(onDone = { onEvent(WelcomeStore.Intent.OnLoginClicked) }),
                    enabled = !state.isLoading,
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
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onEvent(WelcomeStore.Intent.OnLoginClicked) },
            enabled = state.isLoginButtonEnabled,
            modifier = Modifier.fillMaxWidth(),
        ) {
            AnimatedContent(
                targetState = state.isLoading,
                label = "LoginButtonContent"
            ) { isLoading ->
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.sizeIn(maxHeight = 24.dp, maxWidth = 24.dp)
                    )
                } else {
                    Text("Login")
                }
            }
        }
    }
}

const val DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION = 0.55f
