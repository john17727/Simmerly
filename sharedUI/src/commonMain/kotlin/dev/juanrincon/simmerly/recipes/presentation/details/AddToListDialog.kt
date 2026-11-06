package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.foundation.layout.RowScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Checkbox
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListRowUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.theme.dmSerifDisplayFontFamily

/**
 * Desktop version of [AddToListSheet], from the Simmerly Desktop design: one dialog holding the
 * list picker, the servings stepper and the ingredient checklist, so nothing is spread over steps.
 * It drives the same state and ViewModel as the sheet.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun AddToListDialog(
    sheet: AddToListState,
    recipeTitle: String,
    ingredients: List<IngredientUi>,
    onEvent: (RecipeDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val count = sheet.selected.size
    val selectedExistingId = (sheet.target as? AddToListTarget.Existing)?.id
    val hasList = if (sheet.creatingNew) sheet.newListName.isNotBlank() else selectedExistingId != null
    val canConfirm = hasList && count > 0 && !sheet.isSubmitting
    val onDismiss = { onEvent(RecipeDetailsIntent.DismissAddToList) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp,
            modifier = modifier
                .width(560.dp)
                .heightIn(max = 760.dp)
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    when (event.key) {
                        Key.Escape -> onDismiss()
                        Key.Enter, Key.NumPadEnter ->
                            if (canConfirm) onEvent(RecipeDetailsIntent.AddToListConfirm)
                            else return@onPreviewKeyEvent false
                        else -> return@onPreviewKeyEvent false
                    }
                    true
                },
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        "Add to a shopping list",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = dmSerifDisplayFontFamily(),
                            fontSize = 28.sp,
                            lineHeight = 36.sp,
                        ),
                    )
                    Text(
                        recipeTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                // Everything between the header and the footer scrolls when the window is short.
                Column(
                    modifier = Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    ListSection(sheet, recipeTitle, selectedExistingId, onEvent)
                    ServingsRow(sheet.servings, onEvent)
                    IngredientsSection(sheet, ingredients, onEvent)
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextButton(onClick = onDismiss, enabled = !sheet.isSubmitting) { Text("Cancel") }
                    Button(
                        onClick = { onEvent(RecipeDetailsIntent.AddToListConfirm) },
                        enabled = canConfirm,
                    ) {
                        if (sheet.isSubmitting) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text(confirmLabel(sheet.creatingNew, count))
                        }
                    }
                }
            }
        }
    }
}

private fun confirmLabel(isNew: Boolean, count: Int): String = when {
    count == 0 -> "Select ingredients to add"
    isNew && count == 1 -> "Create list and add 1 item"
    isNew -> "Create list and add $count items"
    count == 1 -> "Add 1 item"
    else -> "Add $count items"
}

// region List

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ListSection(
    sheet: AddToListState,
    recipeTitle: String,
    selectedId: String?,
    onEvent: (RecipeDetailsIntent) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionLabel("List")
        val lists = sheet.lists
        if (lists == null) {
            Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // Two equal columns, as in the design; a lone last card keeps its column's width.
            lists.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    pair.forEach { row ->
                        ListCard(
                            row = row,
                            selected = !sheet.creatingNew && row.id == selectedId,
                            onClick = { onEvent(RecipeDetailsIntent.AddToListSelectExisting(row.id)) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                    if (pair.size == 1) Box(Modifier.weight(1f))
                }
            }
        }
        if (sheet.creatingNew) {
            NewListCard(sheet, recipeTitle, onEvent)
        } else {
            TextButton(onClick = { onEvent(RecipeDetailsIntent.AddToListSelectNew) }) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text("New list", modifier = Modifier.padding(start = 8.dp))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ListCard(
    row: AddToListRowUi,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val subtitle = when {
        row.alreadyHasRecipe -> "Already has this recipe"
        row.itemCount == 0 -> "Empty"
        row.itemCount == 1 -> "1 item"
        else -> "${row.itemCount} items"
    }
    SelectableCard(selected = selected, onClick = onClick, modifier = modifier) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                row.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if (row.alreadyHasRecipe) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

/** A radio card: 56dp+, 12dp corners, a 2dp primary border and tint when selected. */
@Composable
private fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = if (selected) colors.primaryContainer else colors.surface,
        border = BorderStroke(if (selected) 2.dp else 1.dp, if (selected) colors.primary else colors.outlineVariant),
        modifier = modifier
            .defaultMinSize(minHeight = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            RadioButton(selected = selected, onClick = null, modifier = Modifier.padding(8.dp))
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun NewListCard(
    sheet: AddToListState,
    recipeTitle: String,
    onEvent: (RecipeDetailsIntent) -> Unit,
) {
    val colors = MaterialTheme.colorScheme
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.primaryContainer,
        border = BorderStroke(2.dp, colors.primary),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RadioButton(selected = true, onClick = null)
                Text("New list", style = MaterialTheme.typography.titleSmall)
            }
            val focusRequester = remember { FocusRequester() }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
            OutlinedTextField(
                value = sheet.newListName,
                onValueChange = { onEvent(RecipeDetailsIntent.AddToListNameChanged(it)) },
                label = { Text("List name") },
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
                trailingIcon = if (sheet.newListName.isNotEmpty()) {
                    {
                        IconButton(onClick = { onEvent(RecipeDetailsIntent.AddToListNameChanged("")) }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear name")
                        }
                    }
                } else null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colors.surface,
                    unfocusedContainerColor = colors.surface,
                ),
                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                itemVerticalAlignment = Alignment.CenterVertically,
            ) {
                Text("Or use", style = MaterialTheme.typography.labelMedium, color = colors.onSurfaceVariant)
                FilterChip(
                    selected = sheet.newListName == recipeTitle,
                    onClick = { onEvent(RecipeDetailsIntent.AddToListNameChanged(recipeTitle)) },
                    label = { Text(recipeTitle, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                    leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp)) },
                )
            }
        }
    }
}

// endregion

// region Servings

@Composable
private fun ServingsRow(servings: Double, onEvent: (RecipeDetailsIntent) -> Unit) {
    val colors = MaterialTheme.colorScheme
    val label = if (servings == 1.0) "1 serving" else "${servings.toInt()} servings"
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = colors.surfaceContainerLow,
        border = BorderStroke(1.dp, colors.outlineVariant),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                "Amounts scale to match",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.onSurfaceVariant,
                modifier = Modifier.weight(1f),
            )
            Row(
                modifier = Modifier.clip(CircleShape).background(colors.surfaceContainer).padding(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StepperButton(
                    icon = Icons.Default.Remove,
                    description = "Fewer servings",
                    enabled = servings > 1,
                    onClick = { onEvent(RecipeDetailsIntent.AddToListRemoveServing) },
                )
                Text(
                    label,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.widthIn(min = 96.dp),
                )
                StepperButton(
                    icon = Icons.Default.Add,
                    description = "More servings",
                    enabled = true,
                    onClick = { onEvent(RecipeDetailsIntent.AddToListAddServing) },
                )
            }
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surface, modifier = Modifier.size(40.dp)) {
        IconButton(onClick = onClick, enabled = enabled) {
            Icon(icon, contentDescription = description, modifier = Modifier.size(20.dp))
        }
    }
}

// endregion

// region Ingredients

@Composable
private fun IngredientsSection(
    sheet: AddToListState,
    ingredients: List<IngredientUi>,
    onEvent: (RecipeDetailsIntent) -> Unit,
) {
    val allSelected = sheet.selected.size == ingredients.size
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                SectionLabel("Ingredients")
                Text(
                    "${sheet.selected.size} of ${ingredients.size}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            TextButton(onClick = { onEvent(RecipeDetailsIntent.AddToListToggleAll) }) {
                Text(if (allSelected) "Clear all" else "Select all")
            }
        }
        ingredients.forEachIndexed { index, ingredient ->
            val on = index in sheet.selected
            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 44.dp)
                    .toggleable(
                        value = on,
                        role = Role.Checkbox,
                        onValueChange = { onEvent(RecipeDetailsIntent.AddToListToggleIngredient(index)) },
                    ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Checkbox(checked = on, onCheckedChange = null, modifier = Modifier.padding(8.dp))
                val color = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                Column(modifier = Modifier.padding(vertical = 8.dp)) {
                    Text(
                        text = listOfNotNull(ingredient.formattedQuantity, ingredient.formattedDisplay).joinToString(" "),
                        style = MaterialTheme.typography.bodyLarge,
                        color = color,
                    )
                    ingredient.note?.takeIf { it.isNotBlank() }?.let { note ->
                        Text(
                            note,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}

// endregion
