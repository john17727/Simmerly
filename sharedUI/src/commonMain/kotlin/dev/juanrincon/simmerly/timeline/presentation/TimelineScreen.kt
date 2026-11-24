package dev.juanrincon.simmerly.timeline.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.EmptyMessage
import dev.juanrincon.simmerly.core.presentation.ErrorMessage
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType
import dev.juanrincon.simmerly.timeline.presentation.models.TimelineEventUi
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineIntent
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineSideEffect
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/** The recipe's timeline: last made, type filters, sort, and the entries on a rail. */
@Composable
fun TimelineScreen(
    recipeId: String,
    onNavigateBack: () -> Unit,
    onEditEvent: (eventId: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = koinViewModel { parametersOf(recipeId) },
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is TimelineSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.asStringSuspend())
        }
    }
    TimelineContent(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onEditEvent = onEditEvent,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun TimelineContent(
    state: TimelineState,
    onEvent: (TimelineIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onEditEvent: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Box(modifier = modifier.fillMaxSize().background(scheme.surface)) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 4.dp, end = 16.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to recipe")
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Timeline", style = MaterialTheme.typography.titleLarge)
                    if (state.recipeName.isNotBlank()) {
                        Text(
                            state.recipeName,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                        )
                    }
                }
            }
            if (state.isRefreshing) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }

                state.hasError -> ErrorMessage(
                    message = "Couldn't load the timeline. Check your connection and try again.",
                    onAction = { onEvent(TimelineIntent.Refresh) },
                )

                else -> PullToRefreshBox(
                    isRefreshing = state.isPullRefreshing,
                    onRefresh = { onEvent(TimelineIntent.Refresh) },
                    modifier = Modifier.fillMaxSize(),
                ) {
                    TimelineList(state, onEvent, onEditEvent)
                }
            }
        }
        SnackbarHost(snackbarHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }

    state.pendingDeleteId?.let {
        AlertDialog(
            onDismissRequest = { onEvent(TimelineIntent.DeleteDismissed) },
            title = { Text("Delete this entry?") },
            text = { Text("It will be removed from the recipe's timeline for everyone. This can't be undone.") },
            confirmButton = {
                TextButton(
                    onClick = { onEvent(TimelineIntent.DeleteConfirmed) },
                    enabled = !state.isDeleting,
                ) { Text("Delete", color = scheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { onEvent(TimelineIntent.DeleteDismissed) }) { Text("Cancel") }
            },
        )
    }
}

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
private fun TimelineList(
    state: TimelineState,
    onEvent: (TimelineIntent) -> Unit,
    onEditEvent: (String) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val events = state.visibleEvents
    val listState = rememberLazyListState()
    LoadMoreEffect(listState, events.size) { onEvent(TimelineIntent.LoadMore) }

    // Wide windows keep the phone's single column, centred, rather than stretching it.
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            state = listState,
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.widthIn(max = 720.dp).fillMaxSize(),
        ) {
            item {
                state.lastMade?.let { lastMade ->
                    Column(
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 4.dp).fillMaxWidth()
                            .background(scheme.surfaceContainerLow, RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text("Last made", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
                        Text(lastMade, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.horizontalScroll(rememberScrollState())
                        .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
                ) {
                    TimelineEventType.entries.forEach { type ->
                        FilterChip(
                            selected = type in state.visibleTypes,
                            onClick = { onEvent(TimelineIntent.ToggleType(type)) },
                            label = { Text(type.label()) },
                        )
                    }
                }
            }
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp)
                ) {
                    Text(
                        if (events.size == 1) "1 event" else "${events.size} events",
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f),
                    )
                    TextButton(onClick = { onEvent(TimelineIntent.ToggleSort) }) {
                        Icon(
                            if (state.newestFirst) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            modifier = Modifier.width(20.dp),
                        )
                        Text(
                            if (state.newestFirst) "Newest first" else "Oldest first",
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
            }
            if (events.isEmpty()) {
                item {
                    EmptyMessage(
                        if (state.events.isEmpty()) "Nothing on the timeline yet."
                        else "No events match these filters. Turn a type back on to see more."
                    )
                }
            }
            itemsIndexed(events, key = { _, e -> e.id }) { index, event ->
                TimelineRow(
                    event = event,
                    isLast = index == events.lastIndex,
                    onEdit = { onEditEvent(event.id) },
                    onDelete = { onEvent(TimelineIntent.DeleteClicked(event.id)) },
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            if (state.isLoadingMore) {
                item {
                    Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.width(24.dp).height(24.dp))
                    }
                }
            }
        }
    }
}

/** Asks for the next page once the last few rows come into view. */
@Composable
private fun LoadMoreEffect(listState: LazyListState, itemCount: Int, onLoadMore: () -> Unit) {
    LaunchedEffect(listState, itemCount) {
        snapshotFlow {
            val info = listState.layoutInfo
            info.totalItemsCount > 0 && (info.visibleItemsInfo.lastOrNull()?.index ?: 0) >= info.totalItemsCount - 3
        }.collect { nearEnd -> if (nearEnd) onLoadMore() }
    }
}

@Composable
private fun TimelineRow(
    event: TimelineEventUi,
    isLast: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            TypeDot(event.type)
            Box(
                Modifier.weight(1f).width(2.dp).padding(vertical = 4.dp)
                    .background(if (isLast) androidx.compose.ui.graphics.Color.Transparent else scheme.outlineVariant)
            )
        }
        if (event.type == TimelineEventType.System) {
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f).padding(top = 6.dp, bottom = 16.dp)
            ) {
                Text(event.subject, style = MaterialTheme.typography.bodyMedium, color = scheme.onSurfaceVariant)
                Text(event.date, style = MaterialTheme.typography.bodySmall, color = scheme.onSurfaceVariant)
            }
        } else {
            TimelineEntryCard(
                subject = event.subject,
                date = event.date,
                initials = event.authorInitials,
                avatarUrl = event.authorImage,
                photo = event.image,
                message = event.message,
                modifier = Modifier.weight(1f).padding(bottom = 16.dp),
                trailing = { if (event.canEdit) EntryMenu(onEdit, onDelete) },
            )
        }
    }
}

@Composable
private fun EntryMenu(onEdit: () -> Unit, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        IconButton(onClick = { expanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Event options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = { expanded = false; onEdit() },
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                },
                onClick = { expanded = false; onDelete() },
            )
        }
    }
}
