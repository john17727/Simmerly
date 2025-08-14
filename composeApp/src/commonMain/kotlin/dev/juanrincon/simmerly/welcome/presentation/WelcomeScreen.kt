package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
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
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
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
    val keyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(state.isLoading) {
        if (state.isLoading) {
            keyboardController?.hide()
        }
    }
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
                Logo(modifier = Modifier.fillMaxWidth())
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Login(state, onEvent, modifier = Modifier.fillMaxWidth())
                }
            }
        } else {
            Row(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
                Column(
                    modifier = Modifier.fillMaxHeight().weight(2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Logo(modifier = Modifier.weight(3f))
                }
                Column(
                    modifier = Modifier.padding(16.dp).background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                        .fillMaxHeight().weight(3f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Login(
                        state,
                        onEvent,
                        modifier = Modifier.fillMaxWidth(DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION)
                    )
                }
            }
        }
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun Login(
    state: WelcomeStore.State,
    onEvent: (WelcomeStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = state.serverAddress,
        onValueChange = { onEvent(WelcomeStore.Intent.OnServerAddressChanged(it)) },
        modifier = modifier.padding(16.dp),
        label = { Text("Server Address") },
        enabled = !state.isLoading,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    OutlinedTextField(
        value = state.username,
        onValueChange = { onEvent(WelcomeStore.Intent.OnUsernameChanged(it)) },
        modifier.padding(horizontal = 16.dp),
        label = { Text("Username/Email") },
        enabled = !state.isLoading,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
    )
    var passwordVisible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = state.password,
        onValueChange = { onEvent(WelcomeStore.Intent.OnPasswordChanged(it)) },
        modifier.padding(horizontal = 16.dp),
        label = { Text("Password") },
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { onEvent(WelcomeStore.Intent.OnLoginClicked) }),
        enabled = !state.isLoading,
        trailingIcon = {
            val image = if (passwordVisible)
                Icons.Filled.Visibility
            else Icons.Filled.VisibilityOff
            val description = if (passwordVisible) "Hide password" else "Show password"
            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                Icon(imageVector = image, description)
            }
        }
    )
    Button(
        onClick = { onEvent(WelcomeStore.Intent.OnLoginClicked) },
        enabled = state.isLoginButtonEnabled,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        AnimatedContent(targetState = state.isLoading, label = "LoginButtonContent") { isLoading ->
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
const val DESKTOP_ELEMENTS_MAX_WIDTH_FRACTION = 0.55f
