package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/** Six rows show at once; any more scroll inside the card. */
private val SUGGESTION_ROW_HEIGHT = 48.dp
private const val VISIBLE_SUGGESTIONS = 6

/** The pinned field for adding items. Free text is resolved by the ViewModel, so this only
 * reports typing, focus and submits. */
@Composable
fun AddItemBar(
    text: String,
    isAdding: Boolean,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onFocusChanged: (Boolean) -> Unit,
) {
    var focused by remember { mutableStateOf(false) }
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(modifier = Modifier.navigationBarsPadding().imePadding()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
            ) {
                TextField(
                    value = text,
                    onValueChange = onTextChanged,
                    placeholder = { Text("Add an item, like 2 lb chicken thighs") },
                    singleLine = true,
                    shape = CircleShape,
                    trailingIcon = if (text.isNotEmpty()) {
                        {
                            IconButton(onClick = { onTextChanged("") }) {
                                Icon(
                                    Icons.Rounded.Close,
                                    contentDescription = "Clear",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                        }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done,
                    ),
                    keyboardActions = KeyboardActions(onDone = { onSubmit() }),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(
                            width = 2.dp,
                            color = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = CircleShape,
                        )
                        .onFocusChanged {
                            focused = it.isFocused
                            onFocusChanged(it.isFocused)
                        }
                        .semantics { contentDescription = "Add an item" },
                )
                IconButton(
                    onClick = onSubmit,
                    enabled = text.isNotBlank() && !isAdding,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                    modifier = Modifier.size(48.dp),
                ) {
                    if (isAdding) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp),
                        )
                    } else {
                        Icon(Icons.Rounded.Add, contentDescription = "Add item")
                    }
                }
            }
        }
    }
}

/** Foods matching what's being typed, floating above the add bar. */
@Composable
fun AddItemSuggestions(
    suggestions: List<FoodOptionUi>,
    query: String,
    highlightedId: String?,
    onPicked: (FoodOptionUi) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = if (suggestions.size == 1) "1 matching ingredient"
                else "${suggestions.size} matching ingredients",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            )
            LazyColumn(
                modifier = Modifier
                    .heightIn(max = SUGGESTION_ROW_HEIGHT * VISIBLE_SUGGESTIONS)
                    .padding(bottom = 4.dp)
                    .semantics { contentDescription = "Matching ingredients" },
            ) {
                items(suggestions, key = { it.id }) { food ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = SUGGESTION_ROW_HEIGHT)
                            .background(
                                if (food.id == highlightedId) MaterialTheme.colorScheme.surfaceContainer
                                else Color.Transparent
                            )
                            .clickable(role = Role.Button) { onPicked(food) }
                            .padding(horizontal = 16.dp),
                    ) {
                        Text(
                            text = highlightMatch(food.name.asString(), query),
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                        )
                        Text(
                            text = food.aisle?.asString() ?: "No aisle",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

/** [name] with the first occurrence of [query] in bold, as the design marks the match. */
private fun highlightMatch(name: String, query: String): AnnotatedString {
    val start = if (query.isBlank()) -1 else name.indexOf(query.trim(), ignoreCase = true)
    if (start < 0) return AnnotatedString(name)
    val end = start + query.trim().length
    return buildAnnotatedString {
        append(name.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(name.substring(start, end)) }
        append(name.substring(end))
    }
}

@Preview
@Composable
private fun AddItemBarEmptyPreview() {
    SimmerlyTheme {
        AddItemBar(text = "", isAdding = false, onTextChanged = {}, onSubmit = {}, onFocusChanged = {})
    }
}

@Preview
@Composable
private fun AddItemBarTypingPreview() {
    SimmerlyTheme {
        AddItemBar(text = "2 cups milk", isAdding = false, onTextChanged = {}, onSubmit = {}, onFocusChanged = {})
    }
}

@Preview
@Composable
private fun AddItemBarAddingPreview() {
    SimmerlyTheme {
        AddItemBar(text = "2 cups milk", isAdding = true, onTextChanged = {}, onSubmit = {}, onFocusChanged = {})
    }
}

@Preview
@Composable
private fun AddItemSuggestionsPreview() {
    fun food(id: String, name: String, aisle: String?) =
        FoodOptionUi(id, UiText.Dynamic(name), aisle?.let { UiText.Dynamic(it) })
    SimmerlyTheme {
        AddItemSuggestions(
            suggestions = listOf(
                food("1", "milk", "Dairy and eggs"),
                food("2", "milk cream", "Dairy and eggs"),
                food("3", "milk powder", "Baking"),
                food("4", "milk breads", "Bakery"),
                food("5", "milkfats", null),
                food("6", "nut milk", "Dairy and eggs"),
                food("7", "oat milk", "Dairy and eggs"),
            ),
            query = "milk",
            highlightedId = "1",
            onPicked = {},
        )
    }
}
