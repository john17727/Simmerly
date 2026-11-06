package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import dev.juanrincon.simmerly.recipes.domain.model.Settings

private val SIDE_SHEET_WIDTH = 360.dp

/**
 * Standard (non-modal) side sheet for foldable and desktop windows: it docks to the right of the
 * recipe and the recipe stays usable beside it, so it sits in a [Row] rather than over a scrim.
 */
@Composable
internal fun StandardSettingsSideSheet(
    visible: Boolean,
    settings: Settings,
    onSettingChanged: (Settings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(expandFrom = Alignment.Start) + fadeIn(),
        exit = shrinkHorizontally(shrinkTowards = Alignment.Start) + fadeOut(),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .width(SIDE_SHEET_WIDTH)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            SettingsSheetContent(
                settings = settings,
                onSettingChanged = onSettingChanged,
                onClose = onClose,
                modifier = Modifier.weight(1f).fillMaxHeight().statusBarsPadding()
            )
        }
    }
}

/**
 * Modal side sheet for phones: slides in from the right over a scrim and blocks the recipe until
 * it is dismissed (scrim tap, close button or back).
 */
@Composable
internal fun ModalSettingsSideSheet(
    settings: Settings,
    onSettingChanged: (Settings) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = slideInHorizontally { it } + fadeIn(),
                exit = slideOutHorizontally { it } + fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp),
                    // Swallows taps so they don't fall through to the scrim.
                    modifier = Modifier
                        .fillMaxHeight()
                        .widthIn(max = 320.dp)
                        .fillMaxWidth(0.85f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    SettingsSheetContent(
                        settings = settings,
                        onSettingChanged = onSettingChanged,
                        onClose = onDismiss,
                        modifier = Modifier.statusBarsPadding().navigationBarsPadding()
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSheetContent(
    settings: Settings,
    onSettingChanged: (Settings) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 72.dp)
                .padding(start = 24.dp, end = 8.dp)
        ) {
            Text(
                "Recipe settings",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close settings")
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp)
        ) {
            SettingRow("Public Recipe", settings.public) { onSettingChanged(settings.copy(public = it)) }
            SettingRow("Show Nutrition Values", settings.showNutrition) {
                onSettingChanged(settings.copy(showNutrition = it))
            }
            SettingRow("Disable Comments", settings.disableComments) {
                onSettingChanged(settings.copy(disableComments = it))
            }
            SettingRow("Locked", settings.locked) { onSettingChanged(settings.copy(locked = it)) }
        }
    }
}

@Composable
private fun SettingRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp)
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
