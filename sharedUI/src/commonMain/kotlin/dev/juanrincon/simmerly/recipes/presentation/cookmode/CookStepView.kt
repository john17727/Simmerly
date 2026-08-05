package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.recipes.domain.ParsedDuration
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookStepUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerOrigin
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookPhase
import dev.juanrincon.simmerly.recipes.presentation.shared.IngredientChipRow
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

private const val MAX_PROGRESS_SEGMENTS = 10

/**
 * A single cooking step (design frames 03 and 05 — the same composable, the only difference is
 * whether [CookModeState.timers] is empty). Renders whichever of image / ingredient chips /
 * detected-duration card the step actually has; a bare text-only step degrades cleanly.
 */
@Composable
internal fun CookStepView(
    state: CookModeState,
    onEvent: (CookModeIntent) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val steps = state.steps
    val step = state.currentStep ?: return
    val recipe = state.recipe
    val isLastStep = state.stepIndex == steps.lastIndex
    val nextStepName = steps.getOrNull(state.stepIndex + 1)?.instruction?.summary

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // The sheet hosting this screen draws edge-to-edge with its own insets zeroed out (see
        // BottomSheetSceneStrategy), so topBar/bottomBar below pad themselves against the status
        // and navigation bars directly instead of relying on Scaffold's automatic accounting.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "STEP ${state.stepIndex + 1} OF ${steps.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            recipe.title.asString(),
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                StepProgress(
                    current = state.stepIndex,
                    total = steps.size,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(12.dp))
                TimerRail(
                    timers = state.timers,
                    nowMillis = state.nowMillis,
                    onTimerClick = { onEvent(CookModeIntent.ShowTimerList) },
                    onAddTimerClick = { onEvent(CookModeIntent.ShowNewTimerSheet) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(4.dp))
            }
        },
        bottomBar = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                OutlinedIconButton(
                    onClick = { onEvent(CookModeIntent.PreviousStep) },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous step")
                }
                Button(
                    onClick = { onEvent(CookModeIntent.NextStep) },
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.weight(1f).height(48.dp)
                ) {
                    Text(
                        text = when {
                            isLastStep -> "Finish"
                            nextStepName != null -> "Next: $nextStepName"
                            else -> "Next step"
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        if (isLastStep) Icons.Default.Check else Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        StepBody(
            step = step,
            timers = state.timers,
            timerOptions = state.currentStepTimerOptions,
            selectedOption = state.selectedRangeOptionOrDefault,
            onStartDetectedTimer = { duration ->
                onEvent(CookModeIntent.StartDetectedTimer(duration, step.instruction.summary))
            },
            onSelectRangeOption = { onEvent(CookModeIntent.SelectRangeOption(it)) },
            onStartSelectedRangeTimer = {
                onEvent(CookModeIntent.StartSelectedRangeTimer(step.instruction.summary))
            },
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        )
    }
}

@Composable
private fun StepProgress(current: Int, total: Int, modifier: Modifier = Modifier) {
    if (total <= MAX_PROGRESS_SEGMENTS) {
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = modifier.height(4.dp)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(percent = 50))
                        .background(
                            if (index <= current) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                )
            }
        }
    } else {
        LinearProgressIndicator(
            progress = { (current + 1) / total.toFloat() },
            modifier = modifier.height(4.dp).clip(RoundedCornerShape(percent = 50)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.outlineVariant,
            // Same stop-indicator suppression as MiseEnPlaceView's checklist bar - see there.
            drawStopIndicator = {}
        )
    }
}

@Composable
private fun StepBody(
    step: CookStepUi,
    timers: List<CookTimerUi>,
    timerOptions: List<Duration>,
    selectedOption: Duration?,
    onStartDetectedTimer: (ParsedDuration) -> Unit,
    onSelectRangeOption: (Duration) -> Unit,
    onStartSelectedRangeTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val richTextState = rememberRichTextState()
    LaunchedEffect(step.instruction.text) {
        richTextState.setMarkdown(step.instruction.text)
    }

    val firstDetected = step.detectedDurations.firstOrNull()
    val alreadyStarted = remember(timers, step.instruction.summary) {
        timers.any { it.origin == TimerOrigin.DETECTED && it.label == step.instruction.summary }
    }

    LazyColumn(
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
    ) {
        val image = step.instruction.images.firstOrNull()
        if (image != null) {
            item {
                AsyncImage(
                    model = image,
                    contentDescription = null,
                    // Full width, natural height - matches the step-image convention in
                    // RecipeDetailsSections.kt's InstructionEntry, rather than cropping to a
                    // fixed box that could cut off the parts of the photo that show the step.
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.medium)
                )
            }
        }
        item {
            Text(
                step.instruction.summary,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (step.ingredients.isNotEmpty()) {
            item { IngredientChipRow(step.ingredients, modifier = Modifier.fillMaxWidth()) }
        }
        item {
            RichText(
                state = richTextState,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (firstDetected != null && !alreadyStarted) {
            item {
                if (firstDetected.isRange) {
                    DetectedRangeCard(
                        options = timerOptions,
                        selected = selectedOption,
                        onSelect = onSelectRangeOption,
                        onStart = onStartSelectedRangeTimer,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    DetectedTimerCard(
                        duration = firstDetected,
                        onStart = { onStartDetectedTimer(firstDetected) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/**
 * The detected-range variant of the timer card: a step that says "cook for 15–17 minutes" can't
 * be reduced to one number, so every reasonable duration in the range is offered as a chip with
 * the shortest preselected. [suggestedTimerOptions][dev.juanrincon.simmerly.recipes.domain.suggestedTimerOptions]
 * decides how many that is — a narrow range lists every minute, a wide one coarsens to round steps.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DetectedRangeCard(
    options: List<Duration>,
    selected: Duration?,
    onSelect: (Duration) -> Unit,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Timer,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                "Range detected · ${options.size} timers suggested",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected
                Text(
                    text = formatPresetLength(option),
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .background(
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surface
                            }
                        )
                        .border(
                            1.dp,
                            if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            },
                            MaterialTheme.shapes.small
                        )
                        .clickable { onSelect(option) }
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                )
            }
        }

        Button(
            onClick = onStart,
            enabled = selected != null,
            shape = RoundedCornerShape(percent = 50),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(selected?.let { "Start ${formatTimerLength(it)} timer" } ?: "Start timer")
        }
    }
}

@Composable
private fun DetectedTimerCard(
    duration: ParsedDuration,
    onStart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
            .border(1.dp, MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Icon(
            Icons.Default.Timer,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.secondary,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text("${formatPresetLength(duration.duration)} timer", style = MaterialTheme.typography.titleMedium)
            Text(
                "Detected in this step",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Button(onClick = onStart, shape = RoundedCornerShape(percent = 50)) {
            Text("Start")
        }
    }
}

// region Previews

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun CookStepDetectedRangeLightPreview() {
    SimmerlyTheme {
        CookStepView(
            state = CookModeState(loading = false, recipe = previewCookRecipe, phase = CookPhase.STEPS, stepIndex = 0),
            onEvent = {},
            onExit = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun CookStepDetectedRangeDarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        CookStepView(
            state = CookModeState(loading = false, recipe = previewCookRecipe, phase = CookPhase.STEPS, stepIndex = 0),
            onEvent = {},
            onExit = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun CookStepNoTimersLightPreview() {
    SimmerlyTheme {
        CookStepView(
            state = CookModeState(loading = false, recipe = previewCookRecipe, phase = CookPhase.STEPS, stepIndex = 1),
            onEvent = {},
            onExit = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun CookStepNoTimersDarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        CookStepView(
            state = CookModeState(loading = false, recipe = previewCookRecipe, phase = CookPhase.STEPS, stepIndex = 1),
            onEvent = {},
            onExit = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun CookStepWithTimersLightPreview() {
    SimmerlyTheme {
        CookStepView(
            state = CookModeState(
                loading = false,
                recipe = previewCookRecipe,
                phase = CookPhase.STEPS,
                stepIndex = 2,
                nowMillis = 1_000_000L,
                timers = listOf(
                    CookTimerUi(
                        id = "timer-1",
                        label = "Step 2",
                        total = 9.minutes,
                        deadlineEpochMillis = 1_000_000L + 372_000L,
                        origin = TimerOrigin.DETECTED
                    ),
                    CookTimerUi(
                        id = "timer-2",
                        label = "Guanciale rest",
                        total = 3.minutes,
                        deadlineEpochMillis = 1_000_000L + 108_000L,
                        origin = TimerOrigin.USER
                    )
                )
            ),
            onEvent = {},
            onExit = {}
        )
    }
}

// endregion
