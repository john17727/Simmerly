package dev.juanrincon.simmerly.timeline.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import dev.juanrincon.simmerly.core.presentation.EmptyMessage
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType
import dev.juanrincon.simmerly.timeline.presentation.models.TimelineEventUi
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineIntent
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineState

/**
 * Desktop layout from the design: a 300dp summary column (recipe, last made, filters, sort) beside
 * a zig-zag timeline where entries alternate sides of a centre rail and the opposite side shows
 * the short date.
 */
@Composable
internal fun TimelineDesktopBody(
    state: TimelineState,
    onEvent: (TimelineIntent) -> Unit,
    onEditEvent: (String) -> Unit,
) {
    val events = state.visibleEvents
    Row(
        horizontalArrangement = Arrangement.spacedBy(48.dp),
        modifier = Modifier.fillMaxSize().padding(start = 32.dp, end = 32.dp, top = 32.dp)
    ) {
        SummaryColumn(state, events.size, onEvent, Modifier.width(300.dp))

        val listState = rememberLazyListState()
        LoadMoreEffect(listState, events.size) { onEvent(TimelineIntent.LoadMore) }
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 64.dp),
            modifier = Modifier.weight(1f).widthIn(max = 960.dp).fillMaxHeight(),
        ) {
            if (events.isEmpty()) {
                item {
                    EmptyMessage(
                        if (state.events.isEmpty()) "Nothing on the timeline yet."
                        else "No events match these filters. Turn a type back on to see more."
                    )
                }
            }
            itemsIndexed(events, key = { _, e -> e.id }) { index, event ->
                ZigZagRow(
                    event = event,
                    onLeft = index % 2 == 0,
                    isLast = index == events.lastIndex,
                    onEdit = { onEditEvent(event.id) },
                    onDelete = { onEvent(TimelineIntent.DeleteClicked(event.id)) },
                )
            }
            if (state.isLoadingMore) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    state: TimelineState,
    count: Int,
    onEvent: (TimelineIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Column(verticalArrangement = Arrangement.spacedBy(24.dp), modifier = modifier) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxWidth()
                .background(scheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                .border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (state.recipeImage.isNotBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current).data(state.recipeImage).build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
                            .background(scheme.surfaceContainerHigh),
                    )
                }
                Text(state.recipeName, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            }
            state.lastMade?.let {
                HorizontalDivider(color = scheme.outlineVariant)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("Last made", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                    Text(it, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Show", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { TypeFilters(state, onEvent) }
        }
        HorizontalDivider(color = scheme.outlineVariant)
        CountAndSort(count, state.newestFirst, onEvent)
    }
}

/** One entry: card on one side of the centre rail, its short date (or nothing, for system rows) opposite. */
@Composable
private fun ZigZagRow(
    event: TimelineEventUi,
    onLeft: Boolean,
    isLast: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val isSystem = event.type == TimelineEventType.System

    @Composable
    fun Side(isCardSide: Boolean, modifier: Modifier) {
        val columnIsLeft = if (isCardSide) onLeft else !onLeft
        Box(modifier, contentAlignment = if (columnIsLeft) Alignment.TopEnd else Alignment.TopStart) {
            when {
                isCardSide && !isSystem -> TimelineEntryCard(
                    subject = event.subject,
                    date = event.date,
                    initials = event.authorInitials,
                    avatarUrl = event.authorImage,
                    photo = event.image,
                    message = event.message,
                    imageHeight = 260.dp,
                    modifier = Modifier.padding(bottom = 24.dp),
                    trailing = { if (event.canEdit) EntryMenu(onEdit, onDelete) },
                )

                isCardSide -> Column(
                    horizontalAlignment = if (columnIsLeft) Alignment.End else Alignment.Start,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(top = 10.dp, bottom = 24.dp),
                ) {
                    Text(event.subject, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                    Text(event.date, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
                }

                !isSystem -> Text(
                    event.shortDate,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.onSurfaceVariant,
                    textAlign = if (columnIsLeft) TextAlign.End else TextAlign.Start,
                    modifier = Modifier.padding(top = 16.dp).fillMaxWidth(),
                )
            }
        }
    }

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        Side(isCardSide = onLeft, modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp).padding(top = 8.dp)) {
            TypeDot(event.type)
            Box(
                Modifier.weight(1f).width(2.dp).padding(top = 4.dp)
                    .background(if (isLast) Color.Transparent else scheme.outlineVariant)
            )
        }
        Side(isCardSide = !onLeft, modifier = Modifier.weight(1f))
    }
}
