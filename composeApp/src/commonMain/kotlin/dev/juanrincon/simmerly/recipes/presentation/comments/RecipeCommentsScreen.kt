package dev.juanrincon.simmerly.recipes.presentation.comments

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import dev.juanrincon.simmerly.recipes.presentation.comments.models.CommentUi
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore
import dev.juanrincon.simmerly.theme.SimmerlyTheme

@Composable
fun RecipeCommentsScreen(
    state: RecipeCommentsStore.State,
    onEvent: (RecipeCommentsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(16.dp)) {
        Text(
            text = "Comments",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            items(state.comments, key = { it.id }) { comment ->
                Comment(comment, modifier = Modifier.fillParentMaxWidth().animateItem())
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = state.commentText,
                onValueChange = { onEvent(RecipeCommentsStore.Intent.OnCommentTextChanged(it)) },
                placeholder = { Text(text = "Join the conversation") },
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = { onEvent(RecipeCommentsStore.Intent.OnSendCommentClicked) }) {
                Icon(Icons.AutoMirrored.Default.Send, contentDescription = null)
            }
        }
    }
}

@Composable
private fun Comment(comment: CommentUi, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SubcomposeAsyncImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(comment.image)
                .build(),
            contentDescription = null,
            error = {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    modifier = Modifier.matchParentSize()
                )
            },
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(24.dp).clip(MaterialTheme.shapes.medium)
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = comment.author, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = comment.date,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(text = comment.text, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Composable
fun RecipeCommentsScreenPreview() {
    SimmerlyTheme {
        RecipeCommentsScreen(
            state = RecipeCommentsStore.State(
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
            modifier = Modifier.fillMaxSize()
        )
    }
}