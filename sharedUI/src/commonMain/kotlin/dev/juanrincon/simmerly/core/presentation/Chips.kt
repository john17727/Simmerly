package dev.juanrincon.simmerly.core.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

enum class ChipTone { Tertiary, Neutral }

/**
 * A read-only label chip: tags, ingredients, sources. [emphasis] is an optional bold lead-in
 * (an ingredient's quantity) and [icon] an optional 16dp glyph before the text.
 */
@Composable
fun SimmerlyChip(
    label: String,
    modifier: Modifier = Modifier,
    tone: ChipTone = ChipTone.Tertiary,
    bordered: Boolean = true,
    emphasis: String? = null,
    icon: (@Composable () -> Unit)? = null,
) {
    val container: Color
    val content: Color
    val border: Color
    when (tone) {
        ChipTone.Tertiary -> {
            container = MaterialTheme.colorScheme.tertiaryContainer
            content = MaterialTheme.colorScheme.onTertiaryContainer
            border = MaterialTheme.colorScheme.tertiary
        }

        ChipTone.Neutral -> {
            container = MaterialTheme.colorScheme.surface
            content = MaterialTheme.colorScheme.onSurfaceVariant
            border = MaterialTheme.colorScheme.surfaceContainerHighest
        }
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(container)
            .then(if (bordered) Modifier.border(1.dp, border, MaterialTheme.shapes.small) else Modifier)
            .padding(vertical = 4.dp, horizontal = 8.dp),
    ) {
        CompositionLocalProvider(LocalContentColor provides content) {
            icon?.invoke()
            if (emphasis != null) {
                Text(
                    text = emphasis,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = content,
                )
            }
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = content)
        }
    }
}

/** Standard 16dp glyph for [SimmerlyChip]'s `icon` slot. */
@Composable
fun ChipIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
}

/**
 * A selectable chip drawn to the design rather than with Material's `FilterChip`, whose hover
 * state layer renders on desktop as a heavy grey wash with a pale box left behind the label. Hover
 * and press only tint the chip's own background, like the design's ghost buttons.
 */
@Composable
fun SelectableChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: Role = Role.Tab,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    val pressed by interactionSource.collectIsPressedAsState()
    val container = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        pressed -> MaterialTheme.colorScheme.surfaceContainerHigh
        hovered -> MaterialTheme.colorScheme.surfaceContainer
        else -> Color.Transparent
    }
    val content = if (selected) MaterialTheme.colorScheme.onPrimaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    val shape = RoundedCornerShape(8.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .heightIn(min = 32.dp)
            .clip(shape)
            .background(container, shape)
            .then(
                if (selected) Modifier
                else Modifier.border(1.dp, MaterialTheme.colorScheme.outline, shape)
            )
            .hoverable(interactionSource)
            .selectable(
                selected = selected,
                interactionSource = interactionSource,
                indication = null,
                role = role,
                onClick = onClick,
            )
            .padding(start = if (selected) 8.dp else 16.dp, end = 16.dp),
    ) {
        if (selected) {
            Icon(Icons.Rounded.Check, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
        }
        Text(text = label, style = MaterialTheme.typography.labelLarge, color = content)
    }
}
