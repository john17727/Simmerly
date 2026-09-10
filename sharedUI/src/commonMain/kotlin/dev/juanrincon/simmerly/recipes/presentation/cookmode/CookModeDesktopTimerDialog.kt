package dev.juanrincon.simmerly.recipes.presentation.cookmode

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerPreset
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.NewTimerDraft
import dev.juanrincon.simmerly.theme.Simmerly
import dev.juanrincon.simmerly.theme.dmSerifDisplayFontFamily
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * The desktop replacement for [NewTimerSheet]. Same draft, same intents — but the sheet's looping
 * scroll wheels are a touch idiom that is miserable with a mouse, so the duration is typed or
 * stepped here instead.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun DesktopNewTimerDialog(
    draft: NewTimerDraft,
    presets: List<TimerPreset>,
    stepNumber: Int,
    onDraftChange: (NewTimerDraft) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val totalSeconds = draft.minutes * 60 + draft.seconds
    val canStart = totalSeconds > 0

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.width(496.dp).onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (event.key) {
                    Key.Enter, Key.NumPadEnter -> if (canStart) onConfirm() else return@onPreviewKeyEvent false
                    Key.Escape -> onDismiss()
                    else -> return@onPreviewKeyEvent false
                }
                true
            }
        ) {
            Column(modifier = Modifier.padding(start = 28.dp, end = 28.dp, top = 28.dp, bottom = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "New timer",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = dmSerifDisplayFontFamily(),
                                fontSize = 30.sp,
                                lineHeight = 36.sp
                            )
                        )
                        Text(
                            "Runs alongside your other timers · step $stepNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 22.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
                        .padding(vertical = 20.dp)
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    DurationField(
                        value = draft.minutes,
                        unit = "min",
                        onValueChange = { onDraftChange(draft.copy(minutes = it.coerceIn(0, 180))) },
                        step = 1
                    )
                    Text(
                        ":",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = dmSerifDisplayFontFamily(),
                            fontSize = 40.sp
                        ),
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(bottom = 22.dp)
                    )
                    DurationField(
                        value = draft.seconds,
                        unit = "sec",
                        // Seconds wrap rather than clamp, so stepping past 59 rolls to 0 the way
                        // the artboard's arrows do.
                        onValueChange = { onDraftChange(draft.copy(seconds = (it + 60) % 60)) },
                        step = 5
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (presets.isNotEmpty()) {
                    DialogEyebrow("From this recipe", modifier = Modifier.padding(top = 20.dp, bottom = 10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { preset ->
                            val selected = preset.duration.inWholeSeconds.toInt() == totalSeconds
                            PresetChip(
                                label = preset.label,
                                selected = selected,
                                onClick = {
                                    onDraftChange(
                                        draft.copy(
                                            minutes = preset.duration.inWholeMinutes.toInt(),
                                            seconds = (preset.duration.inWholeSeconds % 60).toInt()
                                        )
                                    )
                                }
                            )
                        }
                    }
                }

                DialogEyebrow("Name", modifier = Modifier.padding(top = 20.dp, bottom = 8.dp))
                OutlinedTextField(
                    value = draft.label,
                    onValueChange = { onDraftChange(draft.copy(label = it)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 24.dp)
                ) {
                    Text(
                        "Enter to start · Esc to close",
                        style = MaterialTheme.typography.bodySmall,
                        color = Simmerly.Neutral500,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = onConfirm,
                        enabled = canStart,
                        shape = CircleShape,
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(ButtonDefaults.IconSize)
                        )
                        Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                        Text("Start ${formatTimer(draft.minutes.minutes + draft.seconds.seconds)}")
                    }
                }
            }
        }
    }
}

@Composable
private fun DurationField(
    value: Int,
    unit: String,
    onValueChange: (Int) -> Unit,
    step: Int,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
    ) {
        StepperButton(
            onClick = { onValueChange(value + step) },
            contentDescription = "More $unit",
            icon = Icons.Default.KeyboardArrowUp
        )
        // A BasicTextField in a plain bordered box rather than an OutlinedTextField: M3's decoration
        // box reserves room for a label and its own vertical padding, which clips a 48sp glyph
        // inside the artboard's 76dp-tall field. The artboard's control has no label anyway.
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 96.dp, height = 76.dp)
                .clip(MaterialTheme.shapes.medium)
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outlineVariant, MaterialTheme.shapes.medium)
        ) {
            BasicTextField(
                value = value.toString().padStart(2, '0'),
                onValueChange = { typed ->
                    val digits = typed.filter { it.isDigit() }.take(3)
                    onValueChange(digits.toIntOrNull() ?: 0)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                textStyle = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = dmSerifDisplayFontFamily(),
                    fontSize = 48.sp,
                    lineHeight = 56.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            )
        }
        Text(
            unit.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.5.sp
            ),
            color = Simmerly.Neutral500
        )
        StepperButton(
            onClick = { onValueChange(value - step) },
            contentDescription = "Fewer $unit",
            icon = Icons.Default.KeyboardArrowDown
        )
    }
}

@Composable
private fun StepperButton(
    onClick: () -> Unit,
    contentDescription: String,
    icon: ImageVector
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(width = 32.dp, height = 26.dp)
            .clip(RoundedCornerShape(6.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PresetChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.surfaceContainerLow
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
            .border(
                1.dp,
                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                MaterialTheme.shapes.small
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = if (selected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun DialogEyebrow(text: String, modifier: Modifier = Modifier) {
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
