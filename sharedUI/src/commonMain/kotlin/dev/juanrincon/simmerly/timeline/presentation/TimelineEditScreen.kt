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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
import dev.juanrincon.simmerly.timeline.domain.PickedImage
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditIntent
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditSideEffect
import dev.juanrincon.simmerly.timeline.presentation.orbit.TimelineEditState
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.extension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

/**
 * Edit one entry. Phones get a full screen; an unfolded foldable gets the preview beside the
 * form; desktop widths get a dialog with the preview aside. Leaving without saving discards edits.
 */
@Composable
fun TimelineEditScreen(
    recipeId: String,
    eventId: String,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimelineEditViewModel = koinViewModel { parametersOf(recipeId, eventId) },
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is TimelineEditSideEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message.asStringSuspend())
            TimelineEditSideEffect.Finished -> onClose()
        }
    }
    TimelineEditContent(state, viewModel::onEvent, onClose, snackbarHostState, modifier)
}

@Composable
internal fun TimelineEditContent(
    state: TimelineEditState,
    onEvent: (TimelineEditIntent) -> Unit,
    onClose: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { onEvent(TimelineEditIntent.DeleteDismissed) },
            title = { Text("Delete this entry?") },
            text = { Text("It will be removed from the recipe's timeline for everyone. This can't be undone.") },
            confirmButton = {
                TextButton(onClick = { onEvent(TimelineEditIntent.DeleteConfirmed) }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { onEvent(TimelineEditIntent.DeleteDismissed) }) { Text("Cancel") } },
        )
    }
    when {
        isTwoPaneWidth() -> Dialog(
            onDismissRequest = onClose,
            properties = DialogProperties(usePlatformDefaultWidth = false),
        ) {
            Column(
                modifier = Modifier.width(800.dp).clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                EditHeader("Edit entry", state.recipeName, onClose, state, onEvent, showSave = false)
                Row(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(32.dp)) {
                    EditForm(state, onEvent, showDelete = false, modifier = Modifier.weight(1f))
                    Column(Modifier.width(280.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Preview", style = MaterialTheme.typography.labelMedium)
                        PreviewCard(state)
                        Text(
                            "This is how it shows on the timeline.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                DialogActions(state, onEvent, onClose)
            }
        }

        isMediumWidthOrWider() -> Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            Row(Modifier.fillMaxSize().statusBarsPadding().imePadding().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Preview", style = MaterialTheme.typography.labelMedium)
                    PreviewCard(state)
                    Text(
                        "Changes show here as you type. Nothing is saved until you tap Save.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Column(Modifier.weight(1f)) {
                    EditHeader("Edit entry", state.recipeName, onClose, state, onEvent, showSave = true)
                    EditForm(state, onEvent, showDelete = true, modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()))
                }
            }
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }

        else -> Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {
            Column(Modifier.fillMaxSize().statusBarsPadding().imePadding()) {
                EditHeader("Edit entry", null, onClose, state, onEvent, showSave = true)
                EditForm(
                    state, onEvent, showDelete = true,
                    modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(16.dp),
                )
            }
            SnackbarHost(snackbarHostState, Modifier.align(Alignment.BottomCenter))
        }
    }
}

@Composable
private fun EditHeader(
    title: String,
    subtitle: String?,
    onClose: () -> Unit,
    state: TimelineEditState,
    onEvent: (TimelineEditIntent) -> Unit,
    showSave: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().heightIn(min = 64.dp).padding(start = 4.dp, end = 16.dp)
    ) {
        IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close without saving") }
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleLarge)
            if (!subtitle.isNullOrBlank()) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
        }
        if (showSave) {
            Button(onClick = { onEvent(TimelineEditIntent.Save) }, enabled = state.canSave) {
                if (state.isSaving) CircularProgressIndicator(Modifier.height(18.dp).width(18.dp), strokeWidth = 2.dp)
                else Text("Save")
            }
        }
    }
}

@Composable
private fun DialogActions(state: TimelineEditState, onEvent: (TimelineEditIntent) -> Unit, onClose: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        TextButton(onClick = { onEvent(TimelineEditIntent.DeleteClicked) }) {
            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
            Text("Delete entry", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 8.dp))
        }
        Box(Modifier.weight(1f))
        TextButton(onClick = onClose) { Text("Cancel") }
        Button(onClick = { onEvent(TimelineEditIntent.Save) }, enabled = state.canSave) {
            Icon(Icons.Default.Check, contentDescription = null)
            Text("Save", modifier = Modifier.padding(start = 8.dp))
        }
    }
}

@Composable
private fun EditForm(
    state: TimelineEditState,
    onEvent: (TimelineEditIntent) -> Unit,
    showDelete: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(20.dp)) {
        if (state.date.isNotBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TypeDot(state.type)
                Text("${state.type.label()} · ${state.date}", style = MaterialTheme.typography.titleSmall)
            }
        }
        OutlinedTextField(
            value = state.subject,
            onValueChange = { onEvent(TimelineEditIntent.SubjectChanged(it)) },
            label = { Text("Subject") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = state.message,
            onValueChange = { onEvent(TimelineEditIntent.MessageChanged(it)) },
            label = { Text("Message") },
            placeholder = { Text("What happened this time?") },
            minLines = 4,
            maxLines = 8,
            modifier = Modifier.fillMaxWidth(),
        )
        PhotoSection(state, onEvent)
        if (showDelete) {
            TextButton(onClick = { onEvent(TimelineEditIntent.DeleteClicked) }) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text("Delete entry", color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun PhotoSection(state: TimelineEditState, onEvent: (TimelineEditIntent) -> Unit) {
    val scheme = MaterialTheme.colorScheme
    val scope = rememberCoroutineScope()
    val onFile: (PlatformFile) -> Unit = { file ->
        scope.launch {
            onEvent(TimelineEditIntent.PhotoPicked(PickedImage(file.readBytes(), file.extension.lowercase().ifEmpty { "jpg" })))
        }
    }
    val picker = rememberFilePickerLauncher(type = FileKitType.Image) { file: PlatformFile? -> file?.let(onFile) }
    val camera = rememberCameraCapture(onFile)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Photo", style = MaterialTheme.typography.labelMedium, color = scheme.onSurfaceVariant)
        if (state.hasPhoto) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(state.pickedImage?.bytes ?: state.existingImage).build(),
                contentDescription = "Current photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(220.dp).clip(RoundedCornerShape(12.dp))
                    .background(scheme.surfaceContainerHigh),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { picker.launch() }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                    Text("Replace", modifier = Modifier.padding(start = 8.dp))
                }
                TextButton(onClick = { onEvent(TimelineEditIntent.RemovePhoto) }) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = scheme.error)
                    Text("Remove photo", color = scheme.error, modifier = Modifier.padding(start = 8.dp))
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
                    .border(1.5.dp, scheme.outline, RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 24.dp)
            ) {
                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = scheme.onSurfaceVariant)
                Text("Add a photo of how it turned out.", style = MaterialTheme.typography.bodyMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    camera?.let {
                        OutlinedButton(onClick = it) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null)
                            Text("Take photo", modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                    OutlinedButton(onClick = { picker.launch() }) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Text(if (camera != null) "Choose from gallery" else "Choose photo", modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewCard(state: TimelineEditState) {
    TimelineEntryCard(
        subject = state.subject.ifBlank { "Untitled entry" },
        date = state.date,
        initials = "",
        avatarUrl = "",
        photo = if (state.removeImage && state.pickedImage == null) null
        else state.pickedImage?.bytes ?: state.existingImage,
        message = state.message.ifBlank { "No message yet" },
        highlighted = true,
    )
}
