package dev.juanrincon.simmerly.initialload.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.collectLatest
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import simmerly.shared.generated.resources.Res
import simmerly.shared.generated.resources.simmerly_logo

@Composable
fun InitialLoadScreen(
    onLoadComplete: () -> Unit,
    viewModel: InitialLoadViewModel = koinViewModel(),
) {
    LaunchedEffect(viewModel.events) {
        viewModel.events.collectLatest { onLoadComplete() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(Res.drawable.simmerly_logo),
            contentDescription = "Simmerly Logo",
            modifier = Modifier.sizeIn(100.dp, 100.dp, 150.dp, 150.dp),
            colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
        )
        LinearProgressIndicator(
            modifier = Modifier
                .fillMaxWidth(0.4f)
                .padding(top = 32.dp),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
        )
    }
}
