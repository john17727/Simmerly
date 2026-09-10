package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.compose.foundation.focusable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookStepUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerOrigin
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.shared.IngredientChipRow
import dev.juanrincon.simmerly.recipes.presentation.shared.TagChip
import dev.juanrincon.simmerly.theme.Simmerly
import dev.juanrincon.simmerly.theme.dmSerifDisplayFontFamily

internal val RAIL_WIDTH = 272.dp
internal val SIDEBAR_WIDTH = 352.dp
private val STAGE_PADDING = 48.dp
private val STAGE_TEXT_MAX_WIDTH = 720.dp
private val STEP_PHOTO_WIDTH = 320.dp
private val STEP_PHOTO_HEIGHT = 214.dp

/**
 * Cook Mode as a desktop console: a step rail, the current step on a large stage, and a timers +
 * ingredients sidebar, all visible at once. The phone flow paginates the same state through
 * [MiseEnPlaceView] / [CookStepView]; here there is room to show the whole cook at a glance, so
 * the cook can jump between steps and tick ingredients off without leaving the step they are on.
 *
 * Renders the steps phase only — [CookModeScreen] keeps the phone layouts for mise en place and
 * the done screen.
 */
@Composable
internal fun CookModeDesktopConsole(
    state: CookModeState,
    onEvent: (CookModeIntent) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    // CookModeState.steps re-parses every instruction's markdown for durations on each access, and
    // all three panes want the list — resolve it once per recipe.
    val steps = remember(state.recipe) { state.steps }
    val step = steps.getOrNull(state.stepIndex) ?: return

    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { event ->
                // The dialog owns Escape and Enter while it is up, and its name field needs to
                // receive a literal "t" — so the console's shortcuts stand down entirely.
                if (event.type != KeyEventType.KeyDown || state.newTimerDraft != null) {
                    return@onPreviewKeyEvent false
                }
                when (event.key) {
                    Key.DirectionRight -> onEvent(CookModeIntent.NextStep)
                    Key.DirectionLeft -> onEvent(CookModeIntent.PreviousStep)
                    Key.T -> onEvent(CookModeIntent.ShowNewTimerSheet)
                    Key.Escape -> onExit()
                    else -> return@onPreviewKeyEvent false
                }
                true
            }
    ) {
        DesktopCookTopBar(state = state, stepCount = steps.size, onExit = onExit)
        Row(modifier = Modifier.fillMaxWidth().weight(1f)) {
            DesktopStepRail(
                state = state,
                steps = steps,
                onJumpToStep = { onEvent(CookModeIntent.JumpToStep(it)) },
                modifier = Modifier.width(RAIL_WIDTH).fillMaxHeight()
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DesktopStepStage(
                state = state,
                step = step,
                nextStep = steps.getOrNull(state.stepIndex + 1),
                onEvent = onEvent,
                modifier = Modifier.weight(1f).fillMaxHeight()
            )
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            DesktopCookSidebar(
                state = state,
                step = step,
                onEvent = onEvent,
                modifier = Modifier.width(SIDEBAR_WIDTH).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun DesktopCookTopBar(
    state: CookModeState,
    stepCount: Int,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth().height(64.dp).padding(start = 12.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            IconButton(onClick = onExit) {
                Icon(Icons.Default.Close, contentDescription = "Leave cook mode")
            }
            Column(modifier = Modifier.widthIn(min = 176.dp)) {
                Eyebrow("Step ${state.stepIndex + 1} of $stepCount")
                Text(
                    state.recipe.title.asString(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(stepCount) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    index < state.stepIndex -> MaterialTheme.colorScheme.tertiary
                                    index == state.stepIndex -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant
                                }
                            )
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(
                    Icons.Default.Visibility,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    "Screen stays awake",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    }
}

/** The design's small caps section label, used across all three panes. */
@Composable
private fun Eyebrow(text: String, modifier: Modifier = Modifier) {
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DesktopStepRail(
    state: CookModeState,
    steps: List<CookStepUi>,
    onJumpToStep: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(start = 14.dp, end = 14.dp, top = 18.dp, bottom = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp).padding(bottom = 16.dp)
        ) {
            AsyncImage(
                model = recipe.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.surfaceContainer)
            )
            Column {
                Eyebrow("The dish")
                Text(
                    recipe.title.asString(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    listOfNotNull(
                        "${steps.size} steps".takeIf { steps.isNotEmpty() },
                        recipe.totalTime
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp)
                .padding(top = 14.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Eyebrow("Steps")
            Text(
                "${formatElapsed(state.elapsedCookingMillis)} elapsed",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        steps.forEach { railStep ->
            StepRailRow(
                step = railStep,
                isCurrent = railStep.index == state.stepIndex,
                isDone = railStep.index < state.stepIndex,
                onClick = { onJumpToStep(railStep.index) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (recipe.tools.isNotEmpty()) {
            RailSection("Tools") {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recipe.tools.forEach { TagChip(name = it.name) }
                }
            }
        }

        RailSection("Shortcuts") {
            Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                ShortcutRow("← →", "Move between steps")
                ShortcutRow("T", "New timer")
                ShortcutRow("Esc", "Leave cook mode")
            }
        }
    }
}

@Composable
private fun RailSection(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp).padding(top = 20.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Eyebrow(title, modifier = Modifier.padding(top = 16.dp, bottom = 10.dp))
        content()
    }
}

@Composable
private fun StepRailRow(
    step: CookStepUi,
    isCurrent: Boolean,
    isDone: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (isCurrent) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isDone -> MaterialTheme.colorScheme.tertiary
                        isCurrent -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.surfaceContainer
                    }
                )
        ) {
            if (isDone) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onTertiary,
                    modifier = Modifier.size(16.dp)
                )
            } else {
                Text(
                    "${step.index + 1}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isCurrent) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                step.instruction.summary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                ),
                color = when {
                    isCurrent -> MaterialTheme.colorScheme.onSurface
                    isDone -> Simmerly.Neutral500
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            // Only steps whose text actually names a duration get a meta line — the app has no
            // per-step estimate to fall back on, and inventing one would be worse than silence.
            step.detectedDurations.firstOrNull()?.let { detected ->
                Text(
                    formatPresetLength(detected.duration),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isCurrent) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        Simmerly.Neutral500
                    }
                )
            }
        }
    }
}

@Composable
private fun ShortcutRow(keys: String, description: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            keys,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = Simmerly.Neutral700,
            modifier = Modifier
                .widthIn(min = 52.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                .padding(horizontal = 6.dp, vertical = 2.dp)
        )
        Text(
            description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** `m:ss` for a wall-clock elapsed duration, rolling into `h:mm:ss` past an hour — a long braise
 * shouldn't read as "184:12". */
private fun formatElapsed(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    val paddedSeconds = seconds.toString().padStart(2, '0')
    return if (hours > 0) {
        "$hours:${minutes.toString().padStart(2, '0')}:$paddedSeconds"
    } else {
        "$minutes:$paddedSeconds"
    }
}

@Composable
private fun DesktopStepStage(
    state: CookModeState,
    step: CookStepUi,
    nextStep: CookStepUi?,
    onEvent: (CookModeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val richTextState = rememberRichTextState()
    LaunchedEffect(step.instruction.text) { richTextState.setMarkdown(step.instruction.text) }

    val firstDetected = step.detectedDurations.firstOrNull()
    // Same dedup rule as CookStepView: a detected timer already running for this step means the
    // callout has done its job and should get out of the way.
    val alreadyStarted = remember(state.timers, step.instruction.summary) {
        state.timers.any {
            it.origin == TimerOrigin.DETECTED && it.label == step.instruction.summary
        }
    }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = STAGE_PADDING)
                .padding(top = 36.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(28.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        step.instruction.summary,
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = dmSerifDisplayFontFamily(),
                            fontSize = 46.sp,
                            lineHeight = 54.sp,
                            letterSpacing = (-0.4).sp
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (step.ingredients.isNotEmpty()) {
                        IngredientChipRow(
                            step.ingredients,
                            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
                        )
                    }
                }
                step.instruction.images.firstOrNull()?.let { image ->
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(STEP_PHOTO_WIDTH)
                            .height(STEP_PHOTO_HEIGHT)
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    )
                }
            }

            RichText(
                state = richTextState,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 21.sp,
                    lineHeight = 34.sp,
                    letterSpacing = 0.1.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(max = STAGE_TEXT_MAX_WIDTH)
            )

            if (firstDetected != null && !alreadyStarted) {
                DetectedTimerCallout(
                    label = state.selectedRangeOptionOrDefault?.let { formatTimerLength(it) }
                        ?: formatTimerLength(firstDetected.duration),
                    onStart = {
                        onEvent(CookModeIntent.StartSelectedRangeTimer(step.instruction.summary))
                    },
                    modifier = Modifier.widthIn(max = STAGE_TEXT_MAX_WIDTH)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        nextStep?.let {
            UpNextCard(
                step = it,
                modifier = Modifier
                    .widthIn(max = STAGE_TEXT_MAX_WIDTH)
                    .padding(horizontal = STAGE_PADDING)
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = STAGE_PADDING)
                .padding(top = 20.dp, bottom = 28.dp)
        ) {
            OutlinedButton(
                onClick = { onEvent(CookModeIntent.PreviousStep) },
                enabled = state.stepIndex > 0,
                shape = CircleShape,
                contentPadding = ButtonDefaults.ContentPadding,
                modifier = Modifier.height(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Text("Back")
            }
            Button(
                onClick = { onEvent(CookModeIntent.NextStep) },
                shape = CircleShape,
                modifier = Modifier.height(48.dp)
            ) {
                Text(if (nextStep == null) "Finish cooking" else "Next step")
                Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(ButtonDefaults.IconSize)
                )
            }
            Text(
                "Or press →",
                style = MaterialTheme.typography.bodySmall,
                color = Simmerly.Neutral500
            )
        }
    }
}

@Composable
private fun DetectedTimerCallout(
    label: String,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(26.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text("$label timer", style = MaterialTheme.typography.titleMedium)
            Text(
                "Detected in this step",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(onClick = onStart, shape = CircleShape, modifier = Modifier.height(40.dp)) {
            Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                modifier = Modifier.size(ButtonDefaults.IconSize)
            )
            Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
            Text("Start $label")
        }
    }
}

@Composable
private fun UpNextCard(step: CookStepUi, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Eyebrow("Up next", modifier = Modifier.padding(bottom = 8.dp))
        Text(
            step.instruction.summary,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Text(
            step.instruction.text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )
    }
}
