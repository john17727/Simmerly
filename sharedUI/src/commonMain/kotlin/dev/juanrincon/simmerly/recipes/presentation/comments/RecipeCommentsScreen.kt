package dev.juanrincon.simmerly.recipes.presentation.comments

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import dev.juanrincon.simmerly.recipes.presentation.comments.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.comments.orbit.RecipeCommentsIntent
import dev.juanrincon.simmerly.recipes.presentation.comments.orbit.RecipeCommentsState
import dev.juanrincon.simmerly.theme.SimmerlyTheme
import org.orbitmvi.orbit.compose.collectAsState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

/**
 * Comments sheet from the "Recipe detail, comments open" design: a tinted side sheet with
 * a serif title and count, a close button, top-aligned comments with initial avatars, and a
 * composer pinned under a rule. Shown in the list-detail scene's extra pane at every width.
 */
@Composable
fun RecipeCommentsPanel(
    recipeId: String,
    onClose: () -> Unit,
    viewModel: RecipeCommentsViewModel = koinViewModel { parametersOf(recipeId) },
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    PanelContent(
        state = state,
        onEvent = viewModel::onEvent,
        onClose = onClose,
        modifier = modifier
    )
}

@Composable
private fun PanelContent(
    state: RecipeCommentsState,
    onEvent: (RecipeCommentsIntent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    // Same tint as the detail cards (the design's #f9f9fa), with a rule down the left edge.
    Row(modifier = modifier.background(scheme.surfaceContainerLow)) {
        // The edge rule only means something when the sheet sits beside the recipe.
        if (isMediumWidthOrWider()) VerticalDivider(color = scheme.outlineVariant)
        PanelBody(state, onEvent, onClose, modifier = Modifier.weight(1f).fillMaxHeight())
    }
}

@Composable
private fun PanelBody(
    state: RecipeCommentsState,
    onEvent: (RecipeCommentsIntent) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = MaterialTheme.colorScheme
    Column(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 72.dp)
                .padding(start = 24.dp, end = 8.dp)
        ) {
            Text(
                "Comments",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close comments")
            }
        }
        HorizontalDivider(color = scheme.outlineVariant)
        val listState = rememberLazyListState()
        // Keep the newest comment in view: on first load and after posting one.
        LaunchedEffect(state.comments.lastOrNull()?.id) {
            if (state.comments.isNotEmpty()) listState.animateScrollToItem(state.comments.lastIndex)
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(state.comments, key = { it.id }) { comment ->
                    PanelComment(comment, modifier = Modifier.animateItem())
                }
            }
            if (state.comments.isEmpty()) {
                Text(
                    "No comments yet. Start the conversation.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = scheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(horizontal = 32.dp)
                )
            }
        }
        HorizontalDivider(color = scheme.outlineVariant)
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(scheme.surface)
                .navigationBarsPadding()
                .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp)
        ) {
            OutlinedTextField(
                value = state.commentText,
                onValueChange = { onEvent(RecipeCommentsIntent.OnCommentTextChanged(it)) },
                placeholder = { Text("Join the conversation") },
                minLines = 3,
                maxLines = 6,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )
            FilledIconButton(
                onClick = { onEvent(RecipeCommentsIntent.OnSendCommentClicked) },
                enabled = state.commentText.isNotBlank(),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = scheme.primaryContainer,
                    contentColor = scheme.onPrimaryContainer
                ),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.AutoMirrored.Default.Send, contentDescription = "Post comment")
            }
        }
    }
}

@Composable
private fun PanelComment(comment: CommentUi, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(36.dp).clip(CircleShape).background(scheme.tertiaryContainer)
        ) {
            Text(
                text = comment.author.firstOrNull()?.uppercase().orEmpty(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = scheme.onTertiaryContainer
            )
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    comment.author,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    comment.date,
                    style = MaterialTheme.typography.labelSmall,
                    color = scheme.onSurfaceVariant
                )
            }
            Text(comment.text, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview
@Composable
fun RecipeCommentsScreenPreview() {
    SimmerlyTheme {
        PanelContent(
            state = RecipeCommentsState(
                comments = listOf(
                    CommentUi(
                        id = "1",
                        author = "Juan Rincon",
                        text = "This is a comment",
                        date = "3 days ago",
                        isAuthor = true,
                        image = ""
                    ),
                    CommentUi(
                        id = "2",
                        author = "Sara Velasco",
                        text = "This is another comment",
                        date = "2 hours ago",
                        isAuthor = false,
                        image = ""
                    ),
                    CommentUi(
                        id = "3",
                        author = "Sara Velasco",
                        text = "This is a very long comment about how long comments are great at testing the ui format for long comments",
                        date = "2 hours ago",
                        isAuthor = false,
                        image = ""
                    )
                )
            ),
            onEvent = {},
            onClose = {},
            modifier = Modifier.fillMaxSize()
        )
    }
}