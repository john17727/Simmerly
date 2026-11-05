package dev.juanrincon.simmerly.core.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/** A centred failure message with a single recovery action ("Try again", "Go back"). */
@Composable
fun ErrorMessage(
    message: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    actionLabel: String = "Try again",
) {
    Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = message,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            OutlinedButton(onClick = onAction) { Text(actionLabel) }
        }
    }
}

/** A short in-list note for a list or section with nothing in it. */
@Composable
fun EmptyMessage(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 48.dp),
    )
}

@Preview
@Composable
private fun ErrorMessagePreview() {
    SimmerlyTheme {
        ErrorMessage(message = "Couldn't load this list.", onAction = {})
    }
}

@Preview
@Composable
private fun EmptyMessagePreview() {
    SimmerlyTheme {
        EmptyMessage(text = "Nothing on this list yet. Add your first item below.")
    }
}
