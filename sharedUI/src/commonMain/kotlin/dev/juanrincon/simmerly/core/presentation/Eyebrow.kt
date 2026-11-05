package dev.juanrincon.simmerly.core.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.juanrincon.simmerly.theme.Simmerly
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/** Small uppercase label that titles a group of content ("Steps", "Timers", "Up next"). */
@Composable
fun Eyebrow(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.5.sp
        ),
        color = Simmerly.Neutral500,
        modifier = modifier
    )
}

@Preview
@Composable
private fun EyebrowPreview() {
    SimmerlyTheme {
        Eyebrow("Up next", modifier = Modifier.padding(16.dp))
    }
}
