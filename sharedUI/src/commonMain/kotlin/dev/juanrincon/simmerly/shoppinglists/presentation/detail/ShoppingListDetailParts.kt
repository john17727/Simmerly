package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.ExpandLess
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.UnfoldLess
import androidx.compose.material.icons.rounded.UnfoldMore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.LinkedRecipeUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ShoppingItemUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailIntent

// Pieces of the list detail screen shared by the phone and desktop layouts.

@Composable
internal fun Attribution(text: UiText, muted: Boolean = false) {
    val color = if (muted) MaterialTheme.colorScheme.onSurfaceVariant
    else MaterialTheme.colorScheme.onTertiaryContainer
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(Icons.Rounded.Restaurant, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Text(
            text = text.asString(),
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

internal fun itemText(quantity: String, name: String, nameIsFood: Boolean) = buildAnnotatedString {
    if (quantity.isNotEmpty()) append("$quantity ")
    if (nameIsFood) {
        withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) { append(name) }
    } else {
        append(name)
    }
}


@Composable
internal fun QuantityStepper(
    quantity: Int,
    recipeName: String,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(CircleShape)
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        IconButton(onClick = onDecrease, modifier = Modifier.size(44.dp)) {
            Icon(
                Icons.Rounded.Remove,
                contentDescription = if (quantity <= 1) "Remove $recipeName from this list"
                else "Fewer servings of $recipeName",
                modifier = Modifier.size(20.dp),
            )
        }
        Text(
            text = quantity.toString(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .widthIn(min = 20.dp)
                .semantics { contentDescription = "Quantity $quantity" },
        )
        IconButton(onClick = onIncrease, modifier = Modifier.size(44.dp)) {
            Icon(Icons.Rounded.Add, contentDescription = "More servings of $recipeName", modifier = Modifier.size(20.dp))
        }
    }
}


@Composable
internal fun SectionsControl(
    sectionCount: Int,
    grouping: ItemGrouping,
    anyExpanded: Boolean,
    onCheckAll: () -> Unit,
    onToggleAll: () -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 4.dp, top = 8.dp),
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier,
    ) {
        Text(
            text = when (grouping) {
                ItemGrouping.ByAisle -> if (sectionCount == 1) "1 aisle" else "$sectionCount aisles"
                ItemGrouping.ByRecipe -> if (sectionCount == 1) "1 section" else "$sectionCount sections"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f),
        )
        // Only shown while there are unchecked items, so it never needs a disabled state.
        TextButton(
            onClick = onCheckAll,
            contentPadding = PaddingValues(start = 8.dp, end = 12.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
        ) {
            Icon(Icons.Rounded.DoneAll, contentDescription = null, modifier = Modifier.size(20.dp))
            Text(
                text = "Check all",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
        TextButton(
            onClick = onToggleAll,
            contentPadding = PaddingValues(start = 8.dp, end = 12.dp),
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)),
        ) {
            Icon(
                if (anyExpanded) Icons.Rounded.UnfoldLess else Icons.Rounded.UnfoldMore,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
            )
            Text(
                text = if (anyExpanded) "Collapse all" else "Expand all",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(start = 6.dp),
            )
        }
    }
}

@Composable
internal fun GroupingToggle(
    selected: ItemGrouping,
    onSelected: (ItemGrouping) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier.fillMaxWidth()) {
        ItemGrouping.entries.forEachIndexed { index, grouping ->
            SegmentedButton(
                selected = grouping == selected,
                onClick = { onSelected(grouping) },
                shape = SegmentedButtonDefaults.itemShape(index, ItemGrouping.entries.size),
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    activeContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
                label = {
                    Text(
                        when (grouping) {
                            ItemGrouping.ByAisle -> "By aisle"
                            ItemGrouping.ByRecipe -> "By recipe"
                        }
                    )
                },
            )
        }
    }
}

@Composable
internal fun CheckedCard(
    items: List<ShoppingItemUi>,
    expanded: Boolean,
    onEvent: (ShoppingListDetailIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth(),
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            ) {
                Text(
                    text = "${items.size} checked",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f),
                )
                TextButton(onClick = { onEvent(ShoppingListDetailIntent.OnUncheckAllClicked) }) {
                    Text(
                        "Uncheck all",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
                IconButton(onClick = { onEvent(ShoppingListDetailIntent.OnDeleteCheckedClicked) }) {
                    Icon(
                        Icons.Rounded.Delete,
                        contentDescription = "Delete checked items",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
                IconButton(onClick = { onEvent(ShoppingListDetailIntent.OnCheckedExpandedToggled) }) {
                    Icon(
                        if (expanded) Icons.Rounded.ExpandLess else Icons.Rounded.ExpandMore,
                        contentDescription = if (expanded) "Hide checked items" else "Show checked items",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            if (expanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)) {
                    items.forEachIndexed { index, item ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        CheckedItemRow(
                            item = item,
                            onUncheck = { onEvent(ShoppingListDetailIntent.OnItemCheckedChanged(item.id, false)) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun CheckedItemRow(item: ShoppingItemUi, onUncheck: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = true, role = Role.Checkbox, onValueChange = { onUncheck() })
            .padding(vertical = 12.dp),
    ) {
        Checkbox(
            checked = true,
            onCheckedChange = null,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary),
            modifier = Modifier.size(24.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = listOf(item.quantityText.asString(), item.name.asString())
                    .filter { it.isNotEmpty() }
                    .joinToString(" "),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = TextDecoration.LineThrough,
            )
            item.attribution?.let { Attribution(it, muted = true) }
        }
    }
}

internal fun progressSummary(remaining: Int, checked: Int): String = when {
    remaining == 0 && checked == 0 -> "No items yet"
    remaining == 0 -> "All $checked checked"
    else -> "$remaining to get · $checked checked"
}

internal fun recipeSummary(recipe: LinkedRecipeUi): String = buildString {
    append(if (recipe.itemCount == 1) "1 item" else "${recipe.itemCount} items")
    when {
        recipe.quantity == 2 -> append(" · doubled")
        recipe.quantity > 2 -> append(" · ×${recipe.quantity}")
    }
}
