package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerOrigin
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerPreset
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.NewTimerDraft
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.time.Duration

// region Rail

/** The row of running-timer chips pinned above every step, plus the "add timer" affordance. Shows
 * a "No timers running" line instead of an empty rail. */
@Composable
fun TimerRail(
    timers: List<CookTimerUi>,
    nowMillis: Long,
    onTimerClick: (String) -> Unit,
    onAddTimerClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        if (timers.isEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.TimerOff,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "No timers running",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(timers, key = { it.id }) { timer ->
                    TimerChip(timer = timer, nowMillis = nowMillis, onClick = { onTimerClick(timer.id) })
                }
            }
        }
        AddTimerButton(onClick = onAddTimerClick)
    }
}

@Composable
private fun AddTimerButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(40.dp)
            .dashedBorder(MaterialTheme.colorScheme.outline, cornerRadius = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Icon(
            Icons.Default.Add,
            contentDescription = "Add timer",
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimerChip(
    timer: CookTimerUi,
    nowMillis: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDetected = timer.origin == TimerOrigin.DETECTED
    val ringColor = if (isDetected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.tertiary
    val borderColor =
        if (isDetected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.outlineVariant
    val containerColor = if (isDetected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(containerColor)
            .border(1.dp, borderColor, MaterialTheme.shapes.small)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, top = 8.dp, end = 12.dp, bottom = 8.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
            TimerRing(
                progress = timer.progress(nowMillis),
                color = ringColor,
                trackColor = borderColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Column {
            Text(
                formatTimer(timer.remaining(nowMillis)),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                timer.label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// endregion

// region Timer list sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerListSheet(
    timers: List<CookTimerUi>,
    nowMillis: Long,
    onDismiss: () -> Unit,
    onAddTimer: () -> Unit,
    onPauseTimer: (String) -> Unit,
    onResumeTimer: (String) -> Unit,
    onCancelTimer: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss, modifier = modifier) {
        Column(modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                Text("Timers", style = MaterialTheme.typography.headlineSmall)
                TextButton(onClick = onAddTimer) { Text("Add timer") }
            }
            Spacer(Modifier.height(12.dp))

            if (timers.isEmpty()) {
                Text(
                    "No timers yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 16.dp)
                )
            } else {
                val ordered = remember(timers, nowMillis) {
                    timers.sortedWith(
                        compareBy(
                            { !it.isFinished(nowMillis) },
                            { it.isPaused },
                            { it.remaining(nowMillis) }
                        )
                    )
                }
                ordered.forEach { timer ->
                    TimerListEntry(
                        timer = timer,
                        nowMillis = nowMillis,
                        onPause = { onPauseTimer(timer.id) },
                        onResume = { onResumeTimer(timer.id) },
                        onCancel = { onCancelTimer(timer.id) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(12.dp))
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Timers keep running if you leave cook mode.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TimerListEntry(
    timer: CookTimerUi,
    nowMillis: Long,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val finished = timer.isFinished(nowMillis)
    val ringColor = if (timer.origin == TimerOrigin.DETECTED) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    val containerColor = when {
        finished -> MaterialTheme.colorScheme.inverseSurface
        timer.origin == TimerOrigin.DETECTED && !timer.isPaused -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (finished) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface
    val subtitleColor = if (finished) {
        MaterialTheme.colorScheme.inverseOnSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val subtitle = when {
        finished -> "${timer.label} · finished"
        timer.isPaused -> "${timer.label} · paused"
        else -> "${timer.label} · ${formatPresetLength(timer.total)}"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(containerColor)
            .padding(16.dp)
    ) {
        if (finished) {
            Icon(
                Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.inversePrimary,
                modifier = Modifier.size(28.dp)
            )
        } else {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(44.dp)) {
                TimerRing(
                    progress = timer.progress(nowMillis),
                    color = ringColor,
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(44.dp),
                    strokeWidth = 3.dp
                )
                Icon(
                    if (timer.isPaused) Icons.Default.Pause else Icons.Default.Timer,
                    contentDescription = null,
                    tint = ringColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(formatTimer(timer.remaining(nowMillis)), style = MaterialTheme.typography.headlineSmall, color = contentColor)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
        }
        if (finished) {
            TextButton(onClick = onCancel) {
                Text("Stop", color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Row {
                IconButton(onClick = if (timer.isPaused) onResume else onPause) {
                    Icon(
                        if (timer.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                        contentDescription = if (timer.isPaused) "Resume" else "Pause",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// endregion

// region New timer sheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewTimerSheet(
    draft: NewTimerDraft,
    presets: List<TimerPreset>,
    onDraftChange: (NewTimerDraft) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Leaving PartiallyExpanded out of enabledValues: this sheet's content (wheel + name
    // field + presets + buttons) is taller than the default partial-expand threshold, which
    // was cutting the bottom off on first open. No handle and no drag-to-dismiss either —
    // Cancel/Start are the only way out.
    val sheetState = rememberBottomSheetState(
        initialValue = SheetValue.Hidden,
        enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded)
    )
    val scope = rememberCoroutineScope()

    // Cancel/Start mutate CookModeState to unmount this composable, which would otherwise cut
    // the sheet's hide animation short. Play it out first and only then tell the caller.
    fun dismissAnimated(after: () -> Unit) {
        scope.launch { sheetState.hide() }.invokeOnCompletion { after() }
    }

    ModalBottomSheet(
        onDismissRequest = { dismissAnimated(onDismiss) },
        sheetState = sheetState,
        dragHandle = null,
        sheetGesturesEnabled = false,
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(bottom = 24.dp)) {
            Text("New timer", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(vertical = 16.dp))

            DurationWheelRow(
                minutes = draft.minutes,
                seconds = draft.seconds,
                onMinutesChange = { onDraftChange(draft.copy(minutes = it)) },
                onSecondsChange = { onDraftChange(draft.copy(seconds = it)) },
                modifier = Modifier.fillMaxWidth().height(188.dp)
            )

            OutlinedTextField(
                value = draft.label,
                onValueChange = { onDraftChange(draft.copy(label = it)) },
                label = { Text("Name") },
                leadingIcon = { Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (presets.isNotEmpty()) {
                Spacer(Modifier.height(16.dp))
                Text(
                    "FROM THIS RECIPE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                PresetChipRow(
                    presets = presets,
                    onPresetSelected = { preset ->
                        onDraftChange(
                            draft.copy(
                                minutes = preset.duration.inWholeMinutes.toInt(),
                                seconds = (preset.duration.inWholeSeconds % 60).toInt()
                            )
                        )
                    }
                )
            }

            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = { dismissAnimated(onDismiss) }, modifier = Modifier.weight(1f)) {
                    Text("Cancel")
                }
                Button(
                    onClick = { dismissAnimated(onConfirm) },
                    enabled = draft.minutes > 0 || draft.seconds > 0,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Start")
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PresetChipRow(
    presets: List<TimerPreset>,
    onPresetSelected: (TimerPreset) -> Unit,
    modifier: Modifier = Modifier
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        presets.forEach { preset ->
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.small)
                    .clickable { onPresetSelected(preset) }
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                Text(
                    preset.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun DurationWheelRow(
    minutes: Int,
    seconds: Int,
    onMinutesChange: (Int) -> Unit,
    onSecondsChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemHeight = 48.dp
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(itemHeight)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), MaterialTheme.shapes.medium)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSizeCompat()
        ) {
            WheelColumn(
                range = 0..59,
                value = minutes,
                itemHeight = itemHeight,
                onValueChange = onMinutesChange,
                modifier = Modifier.width(56.dp).fillMaxHeight()
            )
            Text(
                "min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            WheelColumn(
                range = 0..59,
                value = seconds,
                itemHeight = itemHeight,
                onValueChange = onSecondsChange,
                modifier = Modifier.width(56.dp).fillMaxHeight()
            )
            Text(
                "sec",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/** Number of times the value range repeats in the backing list, so the wheel can scroll through
 * a boundary (e.g. seconds 59 -> 00) without running out of neighbors. Minutes/seconds are
 * modular quantities, so looping is the correct behavior here, not just a visual patch: at 2000
 * loops a user would have to spin through roughly a thousand full wraps in either direction to
 * reach a true edge, which never happens in practice. */
private const val WHEEL_LOOP_COUNT = 2000

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WheelColumn(
    range: IntRange,
    value: Int,
    itemHeight: Dp,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val values = remember(range) { range.toList() }
    val loopStart = remember(values) { WHEEL_LOOP_COUNT / 2 * values.size }
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = loopStart + (value - range.first).coerceIn(0, values.lastIndex)
    )
    val flingBehavior = rememberSnapFlingBehavior(listState)
    val density = LocalDensity.current

    val centerIndex by remember {
        derivedStateOf {
            val itemHeightPx = with(density) { itemHeight.toPx() }
            val offsetItems = (listState.firstVisibleItemScrollOffset / itemHeightPx).roundToInt()
            listState.firstVisibleItemIndex + offsetItems
        }
    }

    LaunchedEffect(centerIndex) {
        val centeredValue = values[centerIndex.mod(values.size)]
        if (centeredValue != value) onValueChange(centeredValue)
    }

    BoxWithConstraints(modifier = modifier) {
        // Content padding must leave exactly half a viewport above/below the item list so that
        // *any* item - including whichever one starts centered - can snap into true visual
        // center under the highlight band. The previous flat `itemHeight` padding under-padded
        // this (188dp viewport, 48dp item -> needs 70dp, not 48dp), which combined with a
        // non-looping list is what left a blank cell above a wheel sitting on a boundary value.
        val sidePadding = ((maxHeight - itemHeight) / 2).coerceAtLeast(0.dp)
        LazyColumn(
            state = listState,
            flingBehavior = flingBehavior,
            contentPadding = PaddingValues(vertical = sidePadding),
            modifier = Modifier.fillMaxSize()
        ) {
            items(count = WHEEL_LOOP_COUNT * values.size) { index ->
                val item = values[index.mod(values.size)]
                val isCenter = index == centerIndex
                Box(modifier = Modifier.height(itemHeight).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = item.toString().padStart(2, '0'),
                        style = if (isCenter) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.titleMedium,
                        color = if (isCenter) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        }
                    )
                }
            }
        }
    }
}

// endregion

// region shared helpers

@Composable
private fun TimerRing(
    progress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 3.dp
) {
    Canvas(modifier = modifier) {
        val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        drawArc(color = trackColor, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke)
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = 360f * progress.coerceIn(0f, 1f),
            useCenter = false,
            style = stroke
        )
    }
}

private fun Modifier.dashedBorder(color: Color, cornerRadius: Dp): Modifier = drawWithContent {
    drawContent()
    val stroke = Stroke(
        width = 1.dp.toPx(),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4.dp.toPx(), 4.dp.toPx()), 0f)
    )
    drawRoundRect(
        color = color,
        cornerRadius = CornerRadius(cornerRadius.toPx()),
        style = stroke
    )
}

private fun Modifier.fillMaxSizeCompat(): Modifier = this.then(Modifier.fillMaxWidth().fillMaxHeight())

private fun formatTimer(duration: Duration): String {
    val totalSeconds = duration.inWholeSeconds.coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}

/** "8 min" / "45 sec" — a coarse, human duration label. Used for the timer-list subtitle here
 * and for the detected-duration card's headline in [CookStepView]. */
internal fun formatPresetLength(duration: Duration): String {
    val minutes = duration.inWholeMinutes
    return if (minutes > 0) "$minutes min" else "${duration.inWholeSeconds} sec"
}

/** "15 minute" / "45 second" — the spelled-out form, for prose like "Start 15 minute timer"
 * where the abbreviated [formatPresetLength] would read as clipped. */
internal fun formatTimerLength(duration: Duration): String {
    val minutes = duration.inWholeMinutes
    return if (minutes > 0) "$minutes minute" else "${duration.inWholeSeconds} second"
}

// endregion
