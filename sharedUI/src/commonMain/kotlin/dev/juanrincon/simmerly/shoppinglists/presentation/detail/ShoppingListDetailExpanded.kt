package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardReturn
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemSectionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.LinkedRecipeUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.SectionKind
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ShoppingItemUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailState
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.ListProgressBar
import dev.juanrincon.simmerly.shoppinglists.presentation.shared.RecipeThumb
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/** The content column's widest; the pane centres it beyond that. */
private val CONTENT_MAX_WIDTH = 920.dp

/** Linked recipe cards share a row until each would be narrower than this. */
private val RECIPE_CARD_MIN_WIDTH = 300.dp

/**
 * The open list on wide windows, from the "Shopping lists, desktop" artboard: the name as a page
 * heading, the add-item field near the top (its suggestions drop down beneath it and follow the
 * keyboard), linked recipes in a grid, and each aisle or recipe section as its own card. Pressing
 * N anywhere outside a text field jumps to the add-item field.
 */
@Composable
internal fun ShoppingListDetailExpanded(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    val rootFocus = remember { FocusRequester() }
    val addFieldFocus = remember { FocusRequester() }
    var addFieldFocused by remember { mutableStateOf(false) }
    // The pane takes focus so the N shortcut reaches it before anything else has been clicked.
    LaunchedEffect(Unit) { rootFocus.requestFocus() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .focusRequester(rootFocus)
            .focusable()
            .onKeyEvent { event ->
                // On release rather than press: by then the key's typed "n" has already been
                // delivered (to the pane, which ignores it), so it doesn't land in the field.
                if (event.type == KeyEventType.KeyUp && event.key == Key.N && !addFieldFocused) {
                    addFieldFocus.requestFocus()
                    true
                } else {
                    false
                }
            },
    ) {
        when {
            state.isLoading -> Unit
            state.name == null && state.error != null -> Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.align(Alignment.Center),
            ) {
                Text(
                    text = state.error?.asString().orEmpty(),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp),
                )
                OutlinedButton(
                    onClick = { onEvent(ShoppingListDetailIntent.OnRefresh) },
                    modifier = Modifier.padding(top = 16.dp),
                ) { Text("Try again") }
            }

            else -> DetailColumn(
                state = state,
                onEvent = onEvent,
                addFieldFocus = addFieldFocus,
                addFieldFocused = addFieldFocused,
                onAddFieldFocusChanged = { focused ->
                    addFieldFocused = focused
                    // Hand focus back to the pane, or the N shortcut has nowhere to arrive once
                    // the field (or its clear button, which then disappears) lets go of it.
                    if (!focused) rootFocus.requestFocus()
                },
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 40.dp, bottom = 24.dp)
                .widthIn(max = 400.dp),
        )
    }

    state.editor?.let { editor ->
        EditItemDialog(
            editor = editor,
            units = state.units,
            foodSuggestions = state.foodSuggestions,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun DetailColumn(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    addFieldFocus: FocusRequester,
    addFieldFocused: Boolean,
    onAddFieldFocusChanged: (Boolean) -> Unit,
) {
    val itemModifier = Modifier.widthIn(max = CONTENT_MAX_WIDTH).fillMaxWidth()
    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp),
        contentPadding = PaddingValues(start = 40.dp, end = 40.dp, top = 24.dp, bottom = 96.dp),
        modifier = Modifier.fillMaxSize(),
    ) {
        item(key = "header") {
            Header(state = state, onEvent = onEvent, modifier = itemModifier)
        }
        item(key = "add") {
            AddItemRow(
                state = state,
                onEvent = onEvent,
                focusRequester = addFieldFocus,
                focused = addFieldFocused,
                onFocusChanged = onAddFieldFocusChanged,
                modifier = itemModifier,
            )
        }
        if (state.linkedRecipes.isNotEmpty()) {
            item(key = "recipes") {
                LinkedRecipesGrid(recipes = state.linkedRecipes, onEvent = onEvent, modifier = itemModifier)
            }
        }
        if (state.sections.isEmpty() && state.checkedItems.isEmpty()) {
            item(key = "empty") {
                Text(
                    text = "Nothing on this list yet. Add your first item above.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = itemModifier.padding(vertical = 48.dp),
                )
            }
            return@LazyColumn
        }
        item(key = "controls") {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = itemModifier) {
                GroupingToggle(
                    selected = state.grouping,
                    onSelected = { onEvent(ShoppingListDetailIntent.OnGroupingSelected(it)) },
                    modifier = Modifier.width(260.dp),
                )
                if (state.sections.isNotEmpty()) {
                    SectionsControl(
                        sectionCount = state.sections.size,
                        grouping = state.grouping,
                        anyExpanded = state.anySectionExpanded,
                        onCheckAll = { onEvent(ShoppingListDetailIntent.OnCheckAllClicked) },
                        onToggleAll = { onEvent(ShoppingListDetailIntent.OnToggleAllSectionsClicked) },
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                }
            }
        }
        items(state.sections, key = { "section-${it.key}" }) { section ->
            SectionCard(
                section = section,
                collapsed = section.key in state.collapsedSections,
                onEvent = onEvent,
                modifier = itemModifier.animateItem(),
            )
        }
        if (state.checkedItems.isNotEmpty()) {
            item(key = "checked") {
                CheckedCard(
                    items = state.checkedItems,
                    expanded = state.checkedExpanded,
                    onEvent = onEvent,
                    modifier = itemModifier,
                )
            }
        }
    }
}

@Composable
private fun Header(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = state.name?.asString().orEmpty(),
                style = MaterialTheme.typography.displaySmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(
                        Icons.Rounded.MoreVert,
                        contentDescription = "List options",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = {
                            menuExpanded = false
                            onEvent(ShoppingListDetailIntent.OnRenameClicked)
                        },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete list", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onEvent(ShoppingListDetailIntent.OnDeleteListClicked)
                        },
                    )
                }
            }
        }
        Text(
            text = progressSummary(state.remainingCount, state.checkedCount),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        ListProgressBar(progress = state.progress)
    }
}

/**
 * The add-item field and its dropdown. ↑ / ↓ move through the suggestions, Enter adds the
 * highlighted one (or what was typed when none is), and Esc closes the dropdown until the text
 * changes again.
 */
@Composable
private fun AddItemRow(
    state: ShoppingListDetailState,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    focusRequester: FocusRequester,
    focused: Boolean,
    onFocusChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val suggestions = state.addSuggestions
    // A finished search for the current text that found nothing; see addSuggestionQuery.
    val noMatches = suggestions.isEmpty() && state.addSuggestionQuery.isNotBlank()
    var dismissedFor by remember { mutableStateOf<String?>(null) }
    var highlighted by remember(suggestions) {
        mutableIntStateOf(suggestions.indexOfFirst { it.id == state.addHighlightedId })
    }
    var fieldWidthPx by remember { mutableIntStateOf(0) }
    val showDropdown = focused && state.addText.isNotBlank() && !state.isAdding &&
        dismissedFor != state.addText && (suggestions.isNotEmpty() || noMatches)

    fun submit() {
        val pick = suggestions.getOrNull(highlighted)
        if (pick != null) {
            onEvent(ShoppingListDetailIntent.OnAddSuggestionPicked(pick))
        } else {
            onEvent(ShoppingListDetailIntent.OnAddItemSubmitted)
        }
    }

    Box(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextField(
                value = state.addText,
                onValueChange = { onEvent(ShoppingListDetailIntent.OnAddTextChanged(it)) },
                placeholder = { Text("Add an item, like 2 lb chicken thighs") },
                leadingIcon = {
                    Icon(Icons.Rounded.AddShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
                },
                trailingIcon = {
                    if (state.addText.isNotEmpty()) {
                        IconButton(onClick = { onEvent(ShoppingListDetailIntent.OnAddTextChanged("")) }) {
                            Icon(Icons.Rounded.Close, contentDescription = "Clear", modifier = Modifier.size(20.dp))
                        }
                    } else {
                        KeyHint("N", contentDescription = "Shortcut: N", modifier = Modifier.padding(end = 12.dp))
                    }
                },
                singleLine = true,
                shape = CircleShape,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                ),
                modifier = Modifier
                    .weight(1f)
                    .onSizeChanged { fieldWidthPx = it.width }
                    .border(
                        width = 2.dp,
                        color = if (focused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = CircleShape,
                    )
                    .focusRequester(focusRequester)
                    .onFocusChanged { onFocusChanged(it.isFocused) }
                    .onPreviewKeyEvent { event ->
                        if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                        when (event.key) {
                            Key.DirectionDown -> {
                                if (showDropdown && suggestions.isNotEmpty()) {
                                    highlighted = (highlighted + 1).coerceAtMost(suggestions.lastIndex)
                                    true
                                } else false
                            }

                            Key.DirectionUp -> {
                                if (showDropdown && suggestions.isNotEmpty()) {
                                    highlighted = (highlighted - 1).coerceAtLeast(-1)
                                    true
                                } else false
                            }

                            Key.Enter, Key.NumPadEnter -> {
                                submit()
                                true
                            }

                            Key.Escape -> {
                                if (showDropdown) {
                                    dismissedFor = state.addText
                                    true
                                } else false
                            }

                            else -> false
                        }
                    }
                    .semantics { contentDescription = "Add an item" },
            )
            FilledTonalButton(
                onClick = ::submit,
                enabled = state.addText.isNotBlank() && !state.isAdding,
                contentPadding = PaddingValues(horizontal = 20.dp),
                modifier = Modifier.height(48.dp),
            ) {
                Text("Add", fontWeight = FontWeight.SemiBold)
            }
        }
        if (showDropdown) {
            val density = LocalDensity.current
            Popup(
                alignment = Alignment.TopStart,
                offset = IntOffset(0, with(density) { 56.dp.roundToPx() }),
                // The field keeps focus and the keyboard while the dropdown is open.
                properties = PopupProperties(focusable = false),
            ) {
                SuggestionsDropdown(
                    suggestions = suggestions,
                    query = state.addSuggestionQuery,
                    typed = state.addText.trim(),
                    highlighted = highlighted,
                    onPicked = { onEvent(ShoppingListDetailIntent.OnAddSuggestionPicked(it)) },
                    onAddAsWritten = { onEvent(ShoppingListDetailIntent.OnAddItemSubmitted) },
                    modifier = Modifier.width(with(density) { fieldWidthPx.toDp() }),
                )
            }
        }
    }
}

@Composable
private fun SuggestionsDropdown(
    suggestions: List<FoodOptionUi>,
    query: String,
    typed: String,
    highlighted: Int,
    onPicked: (FoodOptionUi) -> Unit,
    onAddAsWritten: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        shadowElevation = 6.dp,
        modifier = modifier,
    ) {
        Column {
            Text(
                text = when (suggestions.size) {
                    0 -> "No matching ingredients"
                    1 -> "1 matching ingredient"
                    else -> "${suggestions.size} matching ingredients"
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 4.dp),
            )
            if (suggestions.isEmpty()) {
                DropdownRow(highlighted = true, onClick = onAddAsWritten, minHeight = 48) {
                    Icon(
                        Icons.Rounded.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Add ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(typed) }
                            append(" as written")
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.weight(1f),
                    )
                    AisleLabel(null)
                    ReturnHint(visible = true)
                }
                Text(
                    text = "It goes under No aisle. Edit it later to link a food from your Mealie server.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.heightIn(max = (44 * 6 + 4).dp).padding(bottom = 4.dp)) {
                    items(suggestions.size, key = { suggestions[it].id }) { index ->
                        val food = suggestions[index]
                        DropdownRow(highlighted = index == highlighted, onClick = { onPicked(food) }, minHeight = 44) {
                            Text(
                                text = boldMatch(food.name.asString(), query),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f),
                            )
                            AisleLabel(food.aisle)
                            ReturnHint(visible = index == highlighted)
                        }
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                ) {
                    KeyLegend(keys = listOf("↑", "↓"), label = "to move")
                    KeyLegend(keys = listOf("Enter"), label = "to add")
                    KeyLegend(keys = listOf("Esc"), label = "to close")
                }
            }
        }
    }
}

@Composable
private fun DropdownRow(
    highlighted: Boolean,
    onClick: () -> Unit,
    minHeight: Int,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = minHeight.dp)
            .background(if (highlighted) MaterialTheme.colorScheme.surfaceContainer else Color.Transparent)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        content = content,
    )
}

@Composable
private fun AisleLabel(aisle: UiText?) {
    Text(
        text = aisle?.asString() ?: "No aisle",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

/** The return-key mark on the row Enter would add; an equal gap elsewhere keeps aisles aligned. */
@Composable
private fun ReturnHint(visible: Boolean) {
    if (visible) {
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardReturn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    } else {
        Spacer(Modifier.width(18.dp))
    }
}

@Composable
private fun KeyLegend(keys: List<String>, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        keys.forEach { KeyHint(it) }
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** A key cap, as the design draws keyboard shortcuts. */
@Composable
private fun KeyHint(key: String, modifier: Modifier = Modifier, contentDescription: String? = null) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .heightIn(min = 20.dp)
            .widthIn(min = 20.dp)
            .border(1.dp, MaterialTheme.colorScheme.surfaceContainerHighest, RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
            .padding(horizontal = 5.dp)
            .then(if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier),
    ) {
        Text(
            text = key,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/** [name] with the first occurrence of [query] in bold, as the design marks the match. */
private fun boldMatch(name: String, query: String): AnnotatedString {
    val term = query.trim()
    val start = if (term.isEmpty()) -1 else name.indexOf(term, ignoreCase = true)
    if (start < 0) return AnnotatedString(name)
    return buildAnnotatedString {
        append(name.substring(0, start))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(name.substring(start, start + term.length)) }
        append(name.substring(start + term.length))
    }
}

@Composable
private fun LinkedRecipesGrid(
    recipes: List<LinkedRecipeUi>,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        Text(
            text = "Linked recipes",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
        )
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // Like CSS's auto-fit: as many columns as fit, but never more than there are
            // recipes, so two recipes always split the row between them.
            val fit = ((maxWidth + 8.dp) / (RECIPE_CARD_MIN_WIDTH + 8.dp)).toInt().coerceAtLeast(1)
            val columns = fit.coerceAtMost(recipes.size)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                recipes.chunked(columns).forEach { row ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        row.forEach { recipe ->
                            LinkedRecipeCard(recipe = recipe, onEvent = onEvent, modifier = Modifier.weight(1f))
                        }
                        // Pads a short last row so its cards keep the grid's column width.
                        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun LinkedRecipeCard(
    recipe: LinkedRecipeUi,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        ) {
            RecipeThumb(
                imageUrl = recipe.imageUrl,
                contentDescription = "Recipe photo, ${recipe.name.asString()}",
                size = 48.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.name.asString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = recipeSummary(recipe),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            QuantityStepper(
                quantity = recipe.quantity,
                recipeName = recipe.name.asString(),
                onDecrease = { onEvent(ShoppingListDetailIntent.OnRecipeQuantityChanged(recipe.recipeId, -1)) },
                onIncrease = { onEvent(ShoppingListDetailIntent.OnRecipeQuantityChanged(recipe.recipeId, 1)) },
            )
        }
    }
}

/** One aisle or recipe, as its own outlined card with a header that toggles it. */
@Composable
private fun SectionCard(
    section: ItemSectionUi,
    collapsed: Boolean,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val title = when (section.kind) {
        SectionKind.Aisle, SectionKind.Recipe -> section.title.asString()
        SectionKind.NoAisle -> "No aisle"
        SectionKind.AddedByHand -> "Added by hand"
    }
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier,
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .clickable(role = Role.Button) { onEvent(ShoppingListDetailIntent.OnSectionToggled(section.key)) }
                    .semantics { stateDescription = if (collapsed) "Collapsed" else "Expanded" }
                    .padding(start = 16.dp, end = 12.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                )
                Text(
                    text = section.items.size.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f),
                )
                Icon(
                    if (collapsed) Icons.Rounded.ExpandMore else Icons.Rounded.ExpandLess,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (!collapsed) {
                section.items.forEach { item ->
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    DesktopItemRow(
                        item = item,
                        onCheckedChange = { onEvent(ShoppingListDetailIntent.OnItemCheckedChanged(item.id, it)) },
                        onEdit = { onEvent(ShoppingListDetailIntent.OnEditItemClicked(item.id)) },
                    )
                }
            }
        }
    }
}

/** An item with its source on the right of the row rather than beneath it. */
@Composable
private fun DesktopItemRow(
    item: ShoppingItemUi,
    onCheckedChange: (Boolean) -> Unit,
    onEdit: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val hovered by interactionSource.collectIsHoveredAsState()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .hoverable(interactionSource)
            .background(if (hovered) MaterialTheme.colorScheme.surfaceContainerLow else Color.Transparent)
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp)
                .toggleable(value = item.checked, role = Role.Checkbox, onValueChange = onCheckedChange),
        ) {
            Checkbox(
                checked = item.checked,
                onCheckedChange = null,
                colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
                modifier = Modifier.size(24.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(2.dp), modifier = Modifier.weight(1f)) {
                Text(
                    text = itemText(item.quantityText.asString(), item.name.asString(), item.nameIsFood),
                    style = MaterialTheme.typography.bodyLarge,
                )
                item.note?.let {
                    Text(
                        text = it.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val attribution = item.attribution
            if (attribution != null) {
                Attribution(attribution)
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(
                        Icons.Rounded.EditNote,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp),
                    )
                    Text(
                        text = "Added by hand",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        IconButton(onClick = onEdit) {
            Icon(
                Icons.Rounded.Edit,
                contentDescription = "Edit ${item.name.asString()}",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Preview(widthDp = 1000, heightDp = 1100)
@Composable
private fun ShoppingListDetailExpandedPreview() {
    fun text(value: String) = UiText.Dynamic(value)
    fun item(id: String, qty: String, name: String, note: String? = null, from: String? = null) =
        ShoppingItemUi(id, text(qty), text(name), true, note?.let(::text), from?.let(::text), false)
    SimmerlyTheme {
        Surface {
            ShoppingListDetailExpanded(
                state = ShoppingListDetailState(
                    isLoading = false,
                    name = text("Sunday brunch"),
                    remainingCount = 12,
                    checkedCount = 3,
                    progress = 0.2f,
                    linkedRecipes = listOf(
                        LinkedRecipeUi("r1", text("Shakshuka"), "", 1, 6),
                        LinkedRecipeUi("r2", text("Lemon ricotta pancakes"), "", 2, 5),
                    ),
                    sections = listOf(
                        ItemSectionUi(
                            "produce", SectionKind.Aisle, text("Produce"), listOf(
                                item("1", "1", "yellow onion", "diced", "Shakshuka"),
                                item("2", "4", "lemons", "zested and juiced", "Lemon ricotta pancakes ×2"),
                                item("3", "1 pint", "strawberries"),
                            )
                        ),
                        ItemSectionUi("none", SectionKind.NoAisle, text(""), listOf(item("4", "2 tsp", "baking powder"))),
                    ),
                    checkedItems = listOf(item("5", "1 tsp", "ground cumin", from = "Shakshuka").copy(checked = true)),
                ),
                onEvent = {},
                snackbarHostState = remember { SnackbarHostState() },
            )
        }
    }
}
