package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private const val MAX_STARS = 5

/** Index = star count; 0 is the unrated prompt. */
private val RATING_WORDS = listOf(
    "Tap a star to rate",
    "Not for us",
    "It was fine",
    "Good",
    "Really good",
    "A keeper",
)

/**
 * The phone "Rate this recipe" sheet. Picking stars only changes a local draft; [onSave] commits
 * it (`null` when the draft is cleared) and [onDismiss] closes the sheet either way.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RateRecipeSheet(
    recipeTitle: String,
    currentRating: Double?,
    onSave: (Double?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var draft by rememberSaveable {
        mutableIntStateOf(currentRating?.roundToInt()?.coerceIn(0, MAX_STARS) ?: 0)
    }
    val close: () -> Unit = {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = modifier,
    ) {
        RateRecipeContent(
            recipeTitle = recipeTitle,
            draft = draft,
            onDraftChange = { draft = it },
            onSave = {
                onSave(draft.takeIf { it > 0 }?.toDouble())
                close()
            },
            onCancel = close,
        )
    }
}

@Composable
private fun RateRecipeContent(
    recipeTitle: String,
    draft: Int,
    onDraftChange: (Int) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().navigationBarsPadding()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                "Rate this recipe",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Text(
                recipeTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
        ) {
            for (star in 1..MAX_STARS) {
                val on = star <= draft
                IconButton(
                    onClick = { onDraftChange(star) },
                    modifier = Modifier
                        .size(56.dp)
                        .semantics { selected = star == draft }
                ) {
                    Icon(
                        if (on) Icons.Rounded.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (star == 1) "1 star" else "$star stars",
                        tint = if (on) MaterialTheme.colorScheme.secondary
                        else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(44.dp)
                    )
                }
            }
        }
        Text(
            RATING_WORDS[draft],
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (draft > 0) MaterialTheme.colorScheme.onSurface
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, top = 16.dp, end = 24.dp)
        ) {
            Text(
                "This is your own rating. The stars on the recipe show the average of everyone’s ratings.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )
        }
        Spacer(Modifier.padding(top = 16.dp))
        HorizontalDivider()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 16.dp)
        ) {
            if (draft > 0) {
                TextButton(onClick = { onDraftChange(0) }) { Text("Clear rating") }
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = onCancel) { Text("Cancel") }
            Button(onClick = onSave) { Text("Save") }
        }
    }
}

@Preview
@Composable
private fun RateRecipeContentRatedPreview() {
    SimmerlyTheme {
        Surface {
            RateRecipeContent("Sheet Pan Chicken and Roasted Carrots", 4, {}, {}, {})
        }
    }
}

@Preview
@Composable
private fun RateRecipeContentUnratedPreview() {
    SimmerlyTheme {
        Surface {
            RateRecipeContent("Sheet Pan Chicken and Roasted Carrots", 0, {}, {}, {})
        }
    }
}
