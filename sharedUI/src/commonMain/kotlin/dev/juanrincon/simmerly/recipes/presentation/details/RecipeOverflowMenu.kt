package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * The recipe's overflow menu, anchored to the bottom bar's "More" button. Entries the app has no
 * feature for yet are shown, as designed, but disabled.
 */
@Composable
internal fun RecipeOverflowMenu(
    onAddToShoppingList: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Box(modifier = modifier) {
        IconButton(onClick = { expanded = true }, enabled = enabled) {
            Icon(Icons.Default.MoreVert, contentDescription = "More options")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OverflowItem(
                text = "Add to shopping list",
                icon = Icons.Default.AddShoppingCart,
                highlighted = true,
                onClick = {
                    expanded = false
                    onAddToShoppingList()
                },
            )
            OverflowItem(text = "Add to meal plan", icon = Icons.Default.EditCalendar, enabled = false)
            OverflowItem(text = "Mark as made today", icon = Icons.Default.EventAvailable, enabled = false)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            OverflowItem(text = "Share", icon = Icons.Default.Share, enabled = false)
            OverflowItem(text = "Duplicate", icon = Icons.Default.ContentCopy, enabled = false)
            OverflowItem(text = "Print", icon = Icons.Default.Print, enabled = false)
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            OverflowItem(text = "Delete recipe", icon = Icons.Default.Delete, destructive = true, enabled = false)
        }
    }
}

@Composable
private fun OverflowItem(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    enabled: Boolean = true,
    highlighted: Boolean = false,
    destructive: Boolean = false,
) {
    val colors = MaterialTheme.colorScheme
    val contentColor = when {
        highlighted -> colors.onPrimaryContainer
        destructive -> colors.error
        else -> colors.onSurface
    }
    DropdownMenuItem(
        text = {
            Text(text, fontWeight = if (highlighted) FontWeight.SemiBold else FontWeight.Medium)
        },
        leadingIcon = { Icon(icon, contentDescription = null) },
        onClick = onClick,
        enabled = enabled,
        colors = MenuDefaults.itemColors(
            textColor = contentColor,
            leadingIconColor = if (highlighted || destructive) contentColor else colors.onSurfaceVariant,
        ),
        modifier = if (highlighted) modifier.background(colors.primaryContainer) else modifier,
    )
}
