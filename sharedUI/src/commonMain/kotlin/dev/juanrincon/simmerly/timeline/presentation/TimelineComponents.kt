package dev.juanrincon.simmerly.timeline.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType

/** Icon and colours for an event type's dot on the rail, as in the designs. */
@Composable
internal fun TimelineEventType.dotStyle(): Triple<ImageVector, Color, Color> {
    val scheme = MaterialTheme.colorScheme
    return when (this) {
        TimelineEventType.Comment -> Triple(Icons.Default.Restaurant, scheme.primaryContainer, scheme.onPrimaryContainer)
        TimelineEventType.Info -> Triple(Icons.Default.Info, scheme.tertiaryContainer, scheme.onTertiaryContainer)
        TimelineEventType.System -> Triple(Icons.Default.Settings, scheme.surfaceContainerHigh, scheme.onSurfaceVariant)
    }
}

internal fun TimelineEventType.label(): String = when (this) {
    TimelineEventType.Comment -> "Comment"
    TimelineEventType.Info -> "Info"
    TimelineEventType.System -> "System"
}

@Composable
internal fun TypeDot(type: TimelineEventType, modifier: Modifier = Modifier) {
    val (icon, container, content) = type.dotStyle()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(32.dp).clip(CircleShape).background(container)
    ) {
        Icon(icon, contentDescription = null, tint = content, modifier = Modifier.size(18.dp))
    }
}

/** The author's photo, or their initials on a tinted circle while it loads or when there is none. */
@Composable
internal fun AuthorAvatar(initials: String, imageUrl: String, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    val fallback: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize().background(scheme.primaryContainer)
        ) {
            Text(initials, style = MaterialTheme.typography.labelMedium, color = scheme.onPrimaryContainer)
        }
    }
    Box(modifier = modifier.size(32.dp).clip(CircleShape)) {
        if (imageUrl.isBlank()) {
            fallback()
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current).data(imageUrl).build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                loading = { fallback() },
                error = { fallback() },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

/**
 * The entry card from the designs: author, subject and date, optional photo and message. [photo]
 * is any Coil model (URL string or picked bytes). [trailing] holds the overflow menu.
 */
@Composable
internal fun TimelineEntryCard(
    subject: String,
    date: String,
    initials: String,
    avatarUrl: String,
    photo: Any?,
    message: String?,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
    imageHeight: androidx.compose.ui.unit.Dp = 180.dp,
    trailing: @Composable () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surfaceContainerLow)
            .border(
                width = if (highlighted) 2.dp else 1.dp,
                color = if (highlighted) scheme.primary else scheme.outlineVariant,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AuthorAvatar(initials, avatarUrl)
            Column(modifier = Modifier.weight(1f)) {
                Text(subject, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(date, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
            trailing()
        }
        if (photo != null) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current).data(photo).build(),
                contentDescription = "Photo of the dish from $date",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(imageHeight).clip(RoundedCornerShape(8.dp))
                    .background(scheme.surfaceContainerHigh)
            )
        }
        if (!message.isNullOrBlank()) {
            Text(message, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
        }
    }
}
