package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookPhase
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/**
 * The exit screen (design frame 07). Tapping Done fires [CookModeIntent.FinishCooking], which
 * records last-made and a "Cooked" timeline event (the note as its message) unconditionally, and
 * the star rating only if the cook actually tapped one — see the ViewModel for why.
 */
@Composable
internal fun CookDoneView(
    state: CookModeState,
    onEvent: (CookModeIntent) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val stepCount = state.steps.size
    val elapsedMinutes = elapsedCookingMinutes(state)
    val focusManager = LocalFocusManager.current

    Scaffold(
        // Shrinks the whole Scaffold to leave room for the keyboard, so the bottomBar's Done
        // button and the note field above it end up above it instead of hidden behind it — the
        // sheet's own zeroed insets (below) don't cover the IME, only status/nav bars.
        modifier = modifier.fillMaxSize().imePadding(),
        // The sheet hosting this screen draws edge-to-edge with its own insets zeroed out (see
        // BottomSheetSceneStrategy), so topBar/bottomBar below pad themselves against the status
        // and navigation bars directly instead of relying on Scaffold's automatic accounting.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            IconButton(
                onClick = onExit,
                modifier = Modifier.statusBarsPadding().padding(4.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Button(
                    onClick = {
                        onEvent(CookModeIntent.FinishCooking)
                        onExit()
                    },
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Done")
                }
                TextButton(onClick = onExit, modifier = Modifier.fillMaxWidth()) {
                    Text("Back to the recipe")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                AsyncImage(
                    model = recipe.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().height(260.dp).clip(MaterialTheme.shapes.medium)
                )
            }
            item {
                Column {
                    Text("Plated.", style = MaterialTheme.typography.headlineLarge)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "$stepCount ${if (stepCount == 1) "step" else "steps"}, $elapsedMinutes minutes. " +
                            "Rate it while it is still in front of you.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    for (star in 1..5) {
                        IconButton(
                            onClick = { onEvent(CookModeIntent.SetRating(star)) },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                if (star <= state.rating) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Rate $star stars",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
            item {
                OutlinedTextField(
                    value = state.noteDraft,
                    onValueChange = { onEvent(CookModeIntent.UpdateNote(it)) },
                    placeholder = { Text("Leave a note for next time") },
                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.Comment, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/** Rounds [CookModeState.elapsedCookingMillis] down to whole minutes, but never below one — a
 * recipe finished in forty seconds still reads as "1 minute" rather than "0 minutes". */
private fun elapsedCookingMinutes(state: CookModeState): Long =
    if (state.cookingStartedAtMillis == null) 0L
    else (state.elapsedCookingMillis / 60_000L).coerceAtLeast(1L)

// region Previews

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun CookDoneLightPreview() {
    SimmerlyTheme {
        CookDoneView(
            state = CookModeState(
                loading = false,
                recipe = previewCookRecipe,
                phase = CookPhase.DONE,
                rating = 4,
                cookingStartedAtMillis = 0L,
                cookingFinishedAtMillis = 28 * 60_000L
            ),
            onEvent = {},
            onExit = {}
        )
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun CookDoneDarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        CookDoneView(
            state = CookModeState(
                loading = false,
                recipe = previewCookRecipe,
                phase = CookPhase.DONE,
                rating = 4,
                cookingStartedAtMillis = 0L,
                cookingFinishedAtMillis = 28 * 60_000L
            ),
            onEvent = {},
            onExit = {}
        )
    }
}

// endregion
