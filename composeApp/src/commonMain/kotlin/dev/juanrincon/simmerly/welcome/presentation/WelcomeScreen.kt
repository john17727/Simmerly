package dev.juanrincon.simmerly.welcome.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.simmerly_logo
import simmerly.composeapp.generated.resources.welcome_background

@Composable
fun WelcomeScreen() {
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
        Column(
            modifier = Modifier.fillMaxSize().systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(Res.drawable.simmerly_logo),
                    contentDescription = "Simmerly Logo",
                    modifier = Modifier.sizeIn(100.dp, 100.dp, 150.dp, 150.dp)
                )
                Text(
                    text = "Welcome to Simmerly",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "Your self‑hosted recipe nook.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    label = { Text("Server Address") })
                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(0, 2),
                        onClick = {},
                        selected = true,
                        icon = {},
                        label = { Text("Credentials") },
                    )
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(1, 2),
                        onClick = {},
                        selected = false,
                        icon = {},
                        label = { Text("API Token") },
                    )
                }
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    label = { Text("Username/Email") })
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    label = { Text("Password") })
                Button(
                    onClick = {},
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text("Login")
                }
            }
        }
    }
}