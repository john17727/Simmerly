package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListRowUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListStep
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter

/**
 * The "Add to shopping list" flow: choose a list (or name a new one), then pick which ingredients
 * to add and for how many servings.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AddToListSheet(
    sheet: AddToListState,
    recipeTitle: String,
    ingredients: List<IngredientUi>,
    onEvent: (RecipeDetailsIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = { onEvent(RecipeDetailsIntent.DismissAddToList) },
        sheetState = sheetState,
        modifier = modifier,
    ) {
        AnimatedContent(sheet.step) { step ->
            when (step) {
                AddToListStep.ChooseList -> ChooseListContent(sheet, recipeTitle, onEvent)
                AddToListStep.NewListName -> NewListNameContent(sheet, recipeTitle, onEvent)
                AddToListStep.PickIngredients ->
                    PickIngredientsContent(sheet, recipeTitle, ingredients, onEvent)
            }
        }
    }
}

@Composable
private fun SheetHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    badge: String? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(start = if (onBack == null) 24.dp else 8.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (onBack != null) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, maxLines = 1)
                if (badge != null) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                        Text(
                            badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        )
                    }
                }
            }
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
    }
}

// region Choose a list

@Composable
private fun ChooseListContent(
    sheet: AddToListState,
    recipeTitle: String,
    onEvent: (RecipeDetailsIntent) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        SheetHeader(title = "Add to shopping list", subtitle = recipeTitle)
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ListFilter.entries.forEach { filter ->
                FilterChip(
                    selected = sheet.filter == filter,
                    onClick = { onEvent(RecipeDetailsIntent.AddToListFilterChanged(filter)) },
                    label = { Text(if (filter == ListFilter.Mine) "Mine" else "Household") },
                    leadingIcon = if (sheet.filter == filter) {
                        { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                    } else null,
                )
            }
        }
        LazyColumn(
            modifier = Modifier.heightIn(max = 420.dp).padding(horizontal = 8.dp),
        ) {
            item {
                ListRow(
                    icon = Icons.Default.Add,
                    iconTint = MaterialTheme.colorScheme.onPrimaryContainer,
                    iconBackground = MaterialTheme.colorScheme.primaryContainer,
                    title = "New list",
                    titleColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    subtitle = null,
                    onClick = { onEvent(RecipeDetailsIntent.AddToListNewListClicked) },
                )
            }
            val lists = sheet.lists
            if (lists == null) {
                item {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(lists, key = { it.id }) { row ->
                    ExistingListRow(row) { onEvent(RecipeDetailsIntent.AddToListListChosen(row.id)) }
                }
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = { onEvent(RecipeDetailsIntent.DismissAddToList) }) { Text("Cancel") }
        }
    }
}

@Composable
private fun ExistingListRow(row: AddToListRowUi, onClick: () -> Unit) {
    ListRow(
        icon = Icons.Default.ShoppingCart,
        iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
        iconBackground = MaterialTheme.colorScheme.surfaceContainer,
        title = row.name,
        titleColor = MaterialTheme.colorScheme.onSurface,
        subtitle = when {
            row.alreadyHasRecipe -> "Already has this recipe"
            row.itemCount == 0 -> "Empty"
            row.itemCount == 1 -> "1 item"
            else -> "${row.itemCount} items"
        },
        subtitleHighlight = row.alreadyHasRecipe,
        trailing = { Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
        onClick = onClick,
    )
}

@Composable
private fun ListRow(
    icon: ImageVector,
    iconTint: Color,
    iconBackground: Color,
    title: String,
    titleColor: Color,
    subtitle: String?,
    onClick: () -> Unit,
    subtitleHighlight: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 64.dp)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(shape = RoundedCornerShape(8.dp), color = iconBackground, modifier = Modifier.size(40.dp)) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(22.dp))
            }
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = titleColor,
                maxLines = 1,
            )
            if (subtitle != null) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (subtitleHighlight) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp),
                        )
                    }
                    Text(
                        subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (subtitleHighlight) MaterialTheme.colorScheme.tertiary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        trailing?.invoke()
    }
}

// endregion

// region Name a new list

@Composable
private fun NewListNameContent(
    sheet: AddToListState,
    recipeTitle: String,
    onEvent: (RecipeDetailsIntent) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }
    val canContinue = sheet.newListName.isNotBlank()

    Column(modifier = Modifier.fillMaxWidth()) {
        SheetHeader(
            title = "New list",
            subtitle = "For $recipeTitle",
            onBack = { onEvent(RecipeDetailsIntent.AddToListBack) },
        )
        OutlinedTextField(
            value = sheet.newListName,
            onValueChange = { onEvent(RecipeDetailsIntent.AddToListNameChanged(it)) },
            label = { Text("List name") },
            singleLine = true,
            trailingIcon = if (sheet.newListName.isNotEmpty()) {
                {
                    IconButton(onClick = { onEvent(RecipeDetailsIntent.AddToListNameChanged("")) }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear name")
                    }
                }
            } else null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {
                if (canContinue) onEvent(RecipeDetailsIntent.AddToListNameConfirmed)
            }),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp).focusRequester(focusRequester),
        )
        Row(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text("Or use", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            FilterChip(
                selected = sheet.newListName == recipeTitle,
                onClick = { onEvent(RecipeDetailsIntent.AddToListNameChanged(recipeTitle)) },
                label = { Text(recipeTitle, maxLines = 1) },
                leadingIcon = { Icon(Icons.Default.Restaurant, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.weight(1f, fill = false),
            )
        }
        HorizontalDivider(modifier = Modifier.padding(top = 16.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onEvent(RecipeDetailsIntent.DismissAddToList) }) { Text("Cancel") }
            Button(
                onClick = { onEvent(RecipeDetailsIntent.AddToListNameConfirmed) },
                enabled = canContinue,
            ) {
                Text("Next")
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.padding(start = 8.dp).size(18.dp),
                )
            }
        }
    }
}

// endregion

// region Pick ingredients

@Composable
private fun PickIngredientsContent(
    sheet: AddToListState,
    recipeTitle: String,
    ingredients: List<IngredientUi>,
    onEvent: (RecipeDetailsIntent) -> Unit,
) {
    val target = sheet.target ?: return
    val isNew = target is AddToListTarget.New
    val count = sheet.selected.size
    val allSelected = count == ingredients.size
    val servings = sheet.servings
    val servingsLabel = if (servings == 1.0) "1 serving" else "${servings.toInt()} servings"

    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f)) {
        SheetHeader(
            title = target.name,
            subtitle = "From $recipeTitle",
            onBack = { onEvent(RecipeDetailsIntent.AddToListBack) },
            badge = if (isNew) "New" else null,
        )
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 6.dp, end = 6.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(servingsLabel, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "Amounts scale to match",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                OutlinedIconButton(
                    onClick = { onEvent(RecipeDetailsIntent.AddToListRemoveServing) },
                    enabled = servings > 1,
                ) { Icon(Icons.Default.Remove, contentDescription = "Fewer servings") }
                OutlinedIconButton(
                    onClick = { onEvent(RecipeDetailsIntent.AddToListAddServing) },
                    modifier = Modifier.padding(start = 4.dp),
                ) { Icon(Icons.Default.Add, contentDescription = "More servings") }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                "$count of ${ingredients.size} selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TextButton(onClick = { onEvent(RecipeDetailsIntent.AddToListToggleAll) }) {
                Text(if (allSelected) "Clear all" else "Select all")
            }
        }
        HorizontalDivider()
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            itemsIndexed(ingredients) { index, ingredient ->
                val on = index in sheet.selected
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .toggleable(
                            value = on,
                            role = Role.Checkbox,
                            onValueChange = { onEvent(RecipeDetailsIntent.AddToListToggleIngredient(index)) },
                        )
                        .padding(start = 16.dp, end = 24.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Checkbox(checked = on, onCheckedChange = null, modifier = Modifier.padding(12.dp))
                    Column(modifier = Modifier.padding(vertical = 12.dp)) {
                        val color = if (on) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
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
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
            }
        }
        HorizontalDivider()
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = { onEvent(RecipeDetailsIntent.DismissAddToList) }, enabled = !sheet.isSubmitting) {
                Text("Cancel")
            }
            Button(
                onClick = { onEvent(RecipeDetailsIntent.AddToListConfirm) },
                enabled = count > 0 && !sheet.isSubmitting,
                modifier = Modifier.weight(1f),
            ) {
                if (sheet.isSubmitting) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                    Text(
                        text = when {
                            count == 0 -> "Select ingredients to add"
                            isNew && count == 1 -> "Create list with 1 item"
                            isNew -> "Create list with $count items"
                            count == 1 -> "Add 1 item"
                            else -> "Add $count items"
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
            }
        }
    }
}

// endregion
