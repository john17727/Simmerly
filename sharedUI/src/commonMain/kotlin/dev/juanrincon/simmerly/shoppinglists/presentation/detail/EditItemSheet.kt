package dev.juanrincon.simmerly.shoppinglists.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemEditorUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.UnitOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.orbit.ShoppingListDetailIntent

/** Edits one item's quantity, unit, food and note. Choosing a food files the item under that
 * food's aisle; clearing the food field leaves a note-only item. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditItemSheet(
    editor: ItemEditorUi,
    units: List<UnitOptionUi>,
    foodSuggestions: List<FoodOptionUi>,
    onEvent: (ShoppingListDetailIntent) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = { onEvent(ShoppingListDetailIntent.OnEditorDismissed) },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 24.dp)
                .imePadding(),
        ) {
            Text("Edit item", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = editor.quantity,
                    onValueChange = { onEvent(ShoppingListDetailIntent.OnEditorQuantityChanged(it)) },
                    label = { Text("Quantity") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.width(112.dp),
                )
                UnitPicker(
                    units = units,
                    selectedId = editor.unitId,
                    onSelected = { onEvent(ShoppingListDetailIntent.OnEditorUnitSelected(it)) },
                    modifier = Modifier.weight(1f),
                )
            }
            FoodPicker(
                query = editor.foodQuery,
                suggestions = foodSuggestions,
                onQueryChanged = { onEvent(ShoppingListDetailIntent.OnEditorFoodQueryChanged(it)) },
                onSelected = { onEvent(ShoppingListDetailIntent.OnEditorFoodSelected(it)) },
            )
            OutlinedTextField(
                value = editor.note,
                onValueChange = { onEvent(ShoppingListDetailIntent.OnEditorNoteChanged(it)) },
                label = { Text(if (editor.food == null) "Item" else "Note") },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                modifier = Modifier.fillMaxWidth(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = { onEvent(ShoppingListDetailIntent.OnEditorDeleteClicked) },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) { Text("Delete item") }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { onEvent(ShoppingListDetailIntent.OnEditorSaveClicked) },
                    enabled = !editor.isSaving && (editor.food != null || editor.note.isNotBlank()),
                ) { Text("Save") }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnitPicker(
    units: List<UnitOptionUi>,
    selectedId: String?,
    onSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = units.firstOrNull { it.id == selectedId }
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = modifier) {
        OutlinedTextField(
            value = selected?.name ?: "No unit",
            onValueChange = {},
            readOnly = true,
            label = { Text("Unit") },
            singleLine = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DropdownMenuItem(
                text = { Text("No unit") },
                onClick = { expanded = false; onSelected(null) },
            )
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit.name) },
                    onClick = { expanded = false; onSelected(unit.id) },
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodPicker(
    query: String,
    suggestions: List<FoodOptionUi>,
    onQueryChanged: (String) -> Unit,
    onSelected: (FoodOptionUi?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded && suggestions.isNotEmpty(),
        onExpandedChange = { expanded = it },
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = {
                expanded = true
                onQueryChanged(it)
            },
            label = { Text("Food") },
            placeholder = { Text("Search foods") },
            supportingText = { Text("Foods put the item in their aisle. Leave empty for a plain note.") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable),
        )
        ExposedDropdownMenu(
            expanded = expanded && suggestions.isNotEmpty(),
            onDismissRequest = { expanded = false },
        ) {
            suggestions.forEach { food ->
                DropdownMenuItem(
                    text = { Text(food.name) },
                    onClick = {
                        expanded = false
                        onSelected(food)
                    },
                )
            }
        }
    }
}
