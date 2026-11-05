package dev.juanrincon.simmerly.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class StepperPillSize(
    internal val button: Dp,
    internal val icon: Dp,
    internal val padding: Dp,
) {
    Regular(button = 40.dp, icon = 20.dp, padding = 4.dp),
    Compact(button = 32.dp, icon = 18.dp, padding = 2.dp),
}

/**
 * A "− value +" control on a tinted track. Used for servings and recipe quantities.
 *
 * With [showButtons] false it renders just the label, so callers don't need a separate
 * read-only branch (e.g. recipes whose ingredients can't be scaled).
 */
@Composable
fun StepperPill(
    label: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseDescription: String,
    increaseDescription: String,
    modifier: Modifier = Modifier,
    canDecrease: Boolean = true,
    showButtons: Boolean = true,
    size: StepperPillSize = StepperPillSize.Regular,
    labelMinWidth: Dp = 96.dp,
    labelDescription: String? = null,
) {
    Row(
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(size.padding),
        horizontalArrangement = Arrangement.spacedBy(size.padding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showButtons) {
            StepperPillButton(Icons.Default.Remove, decreaseDescription, onDecrease, canDecrease, size)
        }
        Text(
            label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = labelMinWidth)
                .padding(horizontal = if (showButtons) 0.dp else 12.dp)
                .then(
                    if (labelDescription != null) {
                        Modifier.semantics { contentDescription = labelDescription }
                    } else Modifier
                )
        )
        if (showButtons) {
            StepperPillButton(Icons.Default.Add, increaseDescription, onIncrease, true, size)
        }
    }
}

@Composable
private fun StepperPillButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    enabled: Boolean,
    size: StepperPillSize,
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        modifier = Modifier.size(size.button)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(size.icon))
    }
}
