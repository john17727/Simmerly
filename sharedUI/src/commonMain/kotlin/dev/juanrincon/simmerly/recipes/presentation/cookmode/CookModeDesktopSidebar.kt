package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.juanrincon.simmerly.core.presentation.VerticalScrollbar
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookStepUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.CookTimerUi
import dev.juanrincon.simmerly.recipes.presentation.cookmode.models.TimerOrigin
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.theme.Simmerly
import dev.juanrincon.simmerly.theme.dmSerifDisplayFontFamily

/**
 * The console's right-hand column: every running timer, then the full ingredient list with
 * check-off. On the phone both of these are behind sheets or stranded on the mise en place screen —
 * here they stay in view while the cook works through the steps.
 */
@Composable
internal fun DesktopCookSidebar(
    state: CookModeState,
    step: CookStepUi,
    onEvent: (CookModeIntent) -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val stepIngredientIds = step.ingredients.mapTo(mutableSetOf()) { it.referenceId }

    Column(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 10.dp, top = 18.dp, bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SidebarEyebrow("Timers")
                Text(
                    "Add timer",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clip(MaterialTheme.shapes.small)
                        .clickable { onEvent(CookModeIntent.ShowNewTimerSheet) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            if (state.timers.isEmpty()) {
                Text(
                    "No timers running.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 10.dp)
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(end = 10.dp)
                ) {
                    state.timers.forEach { timer ->
                        DesktopTimerCard(
                            timer = timer,
                            nowMillis = state.nowMillis,
                            onToggle = {
                                onEvent(
                                    if (timer.isPaused) {
                                        CookModeIntent.ResumeTimer(timer.id)
                                    } else {
                                        CookModeIntent.PauseTimer(timer.id)
                                    }
                                )
                            },
                            onDismiss = { onEvent(CookModeIntent.CancelTimer(timer.id)) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                SidebarEyebrow("Ingredients")
                Text(
                    "${state.readyIngredientCount} of ${recipe.ingredients.size} ready",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            if (recipe.isParsed) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    ServingStepper(
                        icon = Icons.Default.Remove,
                        contentDescription = "Fewer servings",
                        enabled = recipe.servings > 1,
                        onClick = { onEvent(CookModeIntent.RemoveServing) }
                    )
                    Text(
                        recipe.formattedServings,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.widthIn(min = 72.dp)
                    )
                    ServingStepper(
                        icon = Icons.Default.Add,
                        contentDescription = "More servings",
                        enabled = true,
                        onClick = { onEvent(CookModeIntent.AddServing) }
                    )
                }
            }
        }

        val ingredientListState = rememberLazyListState()
        Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
            LazyColumn(
                state = ingredientListState,
                contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = 16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(recipe.ingredients, key = { it.referenceId }) { ingredient ->
                    SidebarIngredientRow(
                        ingredient = ingredient,
                        checked = ingredient.referenceId in state.checkedIngredientIds,
                        usedInThisStep = ingredient.referenceId in stepIngredientIds,
                        onToggle = {
                            onEvent(CookModeIntent.ToggleIngredient(ingredient.referenceId))
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            VerticalScrollbar(
                ingredientListState,
                modifier = Modifier.align(Alignment.CenterEnd).fillMaxHeight()
            )
        }
    }
}

@Composable
private fun SidebarEyebrow(text: String, modifier: Modifier = Modifier) {
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

@Composable
private fun ServingStepper(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    OutlinedIconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(30.dp)) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(17.dp))
    }
}

@Composable
private fun DesktopTimerCard(
    timer: CookTimerUi,
    nowMillis: Long,
    onToggle: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val finished = timer.isFinished(nowMillis)
    val fromStep = timer.origin == TimerOrigin.DETECTED

    // A finished timer inverts to the dark card, which is the only alert desktop gets — there is
    // no CookTimerAlerts implementation on the JVM target.
    val container = when {
        finished -> MaterialTheme.colorScheme.inverseSurface
        fromStep -> MaterialTheme.colorScheme.surfaceContainerLow
        else -> MaterialTheme.colorScheme.surface
    }
    val onContainer =
        if (finished) MaterialTheme.colorScheme.inverseOnSurface else MaterialTheme.colorScheme.onSurface
    val border = when {
        finished -> MaterialTheme.colorScheme.inverseSurface
        fromStep -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.outlineVariant
    }
    val accent = when {
        finished -> MaterialTheme.colorScheme.primary
        fromStep -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.tertiary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.medium)
            .background(container)
            .border(1.dp, border, MaterialTheme.shapes.medium)
            .padding(12.dp)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(40.dp)) {
            TimerRing(
                progress = timer.progress(nowMillis),
                color = accent,
                trackColor = if (finished) {
                    MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.25f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant
                },
                modifier = Modifier.size(40.dp)
            )
            Icon(
                imageVector = when {
                    finished -> Icons.Default.NotificationsActive
                    timer.isPaused -> Icons.Default.Pause
                    else -> Icons.Default.Timer
                },
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(17.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                formatTimer(timer.remaining(nowMillis)),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = dmSerifDisplayFontFamily(),
                    fontSize = 27.sp,
                    lineHeight = 31.sp
                ),
                color = if (!finished && timer.isPaused) Simmerly.Neutral500 else onContainer
            )
            Text(
                buildString {
                    append(timer.label)
                    when {
                        finished -> append(" · finished")
                        timer.isPaused -> append(" · paused")
                        else -> append(" · ${formatPresetLength(timer.total)}")
                    }
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (finished) {
                    MaterialTheme.colorScheme.inverseOnSurface.copy(alpha = 0.7f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
        if (!finished) {
            IconButton(onClick = onToggle) {
                Icon(
                    if (timer.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (timer.isPaused) "Resume timer" else "Pause timer",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        IconButton(onClick = onDismiss) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Dismiss timer",
                tint = if (finished) {
                    MaterialTheme.colorScheme.inverseOnSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

@Composable
private fun SidebarIngredientRow(
    ingredient: IngredientUi,
    checked: Boolean,
    usedInThisStep: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                if (usedInThisStep) {
                    MaterialTheme.colorScheme.surfaceContainerLow
                } else {
                    Color.Transparent
                }
            )
            .clickable(onClick = onToggle)
            .padding(10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(top = 2.dp)
                .size(20.dp)
                .clip(MaterialTheme.shapes.extraSmall)
                .then(
                    if (checked) {
                        Modifier.background(MaterialTheme.colorScheme.primary)
                    } else {
                        Modifier.border(
                            2.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraSmall
                        )
                    }
                )
        ) {
            if (checked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ingredient.formattedQuantity?.let { quantity ->
                    Text(
                        quantity,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (checked) {
                            Simmerly.Neutral500
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                        textDecoration = if (checked) TextDecoration.LineThrough else null
                    )
                }
                Text(
                    ingredient.formattedDisplay,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (checked) Simmerly.Neutral500 else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (checked) TextDecoration.LineThrough else null
                )
            }
            ingredient.note?.takeIf { it.isNotBlank() }?.let { note ->
                Text(
                    note,
                    style = MaterialTheme.typography.bodySmall,
                    color = Simmerly.Neutral500
                )
            }
        }
        if (usedInThisStep) {
            Text(
                "This step",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                ),
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
        Spacer(modifier = Modifier.height(0.dp))
    }
}
