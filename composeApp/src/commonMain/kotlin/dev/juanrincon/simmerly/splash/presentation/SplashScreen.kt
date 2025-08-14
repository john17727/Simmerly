package dev.juanrincon.simmerly.splash.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import dev.juanrincon.simmerly.welcome.presentation.Logo
import org.jetbrains.compose.resources.painterResource
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.welcome_background

@Composable
fun SplashScreen() {
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
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Logo(modifier = Modifier.fillMaxWidth())
        }
    }
}