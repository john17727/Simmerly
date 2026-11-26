package dev.juanrincon.simmerly.mealplan.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.EventBusy
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Group
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.StickyNote2
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import dev.juanrincon.simmerly.core.presentation.StepperPill
import dev.juanrincon.simmerly.core.presentation.StepperPillSize
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import kotlinx.datetime.LocalDate

/** The seven-day strip under the header: weekday letter, date, and a dot when something is planned. */
@Composable
fun WeekStrip(
    days: List<LocalDate>,
    selected: LocalDate,
    today: LocalDate,
    plannedDays: Set<LocalDate>,
    onSelect: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = MaterialTheme.colorScheme
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        days.forEach { day ->
            val isSelected = day == selected
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .weight(1f)
                    .height(76.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) scheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent)
                    .clickable { onSelect(day) }
                    .padding(vertical = 8.dp),
            ) {
                Text(
                    day.dayOfWeek.shortName(),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) scheme.onPrimaryContainer else scheme.onSurfaceVariant,
                )
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) scheme.primary else androidx.compose.ui.graphics.Color.Transparent)
                        .then(
                            if (day == today && !isSelected) Modifier.border(1.5.dp, scheme.primary, CircleShape)
                            else Modifier
                        ),
                ) {
                    Text(
                        day.day.toString(),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isSelected) scheme.onPrimary else scheme.onSurface,
                    )
                }
                Box(
                    Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(
                            if (day in plannedDays) scheme.primary else androidx.compose.ui.graphics.Color.Transparent
                        )
                )
            }
        }
    }
}

@Composable
fun OfflineBanner(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    val scheme = MaterialTheme.colorScheme
    Row(
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(scheme.surfaceContainerLow)
            .border(1.dp, scheme.outlineVariant, RoundedCornerShape(12.dp))
            .padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 8.dp),
    ) {
        Icon(
            Icons.Rounded.CloudOff, contentDescription = null,
            tint = scheme.onSurfaceVariant, modifier = Modifier.padding(top = 2.dp).size(20.dp),
        )
        Column(Modifier.weight(1f)) {
            Text("You're offline", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Text(
                "Showing the plan saved on this device. Changes are paused until you reconnect.",
                style = MaterialTheme.typography.bodySmall,
                color = scheme.onSurfaceVariant,
            )
        }
        TextButton(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun TodayBadge(modifier: Modifier = Modifier) {
    val tertiary = MaterialTheme.colorScheme.tertiary
    Text(
        "Today",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onTertiaryContainer,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.tertiaryContainer)
            .border(1.dp, tertiary, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp),
    )
}

/** A dashed "Plan dinner" slot for an empty meal. */
@Composable
fun PlanSlot(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = MaterialTheme.colorScheme.outlineVariant
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .drawBehind {
                drawRoundRect(
                    color = color,
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f)),
                    ),
                )
            },
    ) {
        Icon(Icons.Rounded.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Spacer(Modifier.size(8.dp))
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/**
 * One planned recipe or note. Recipes open on tap; notes don't. [onCook] is set for any-day entries
 * ("Cook today"); the servings stepper only shows for recipes.
 */
@Composable
fun PlanEntryCard(
    entry: MealPlanEntry,
    canEdit: Boolean,
    onOpenRecipe: () -> Unit,
    onServings: (Int) -> Unit,
    onEdit: () -> Unit,
    onAddToList: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    /** Label of the "Cook today" button; null for entries already on a day. */
    cookLabel: String? = null,
    onCook: () -> Unit = {},
) {
    val scheme = MaterialTheme.colorScheme
    val shape = RoundedCornerShape(12.dp)
    var menuOpen by remember { mutableStateOf(false) }
    val recipe = entry.recipe
    val title = recipe?.name ?: entry.title
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.surfaceContainerLow)
            .border(1.dp, scheme.outlineVariant, shape)
            .padding(start = 8.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (recipe != null) {
                SubcomposeAsyncImage(
                    model = recipe.image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    error = { PlaceholderThumb(Icons.Rounded.Restaurant) },
                    loading = { PlaceholderThumb(Icons.Rounded.Restaurant) },
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                )
            } else {
                PlaceholderThumb(Icons.Rounded.StickyNote2)
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                if (entry.isWholeWeek) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(entry.type.icon(), null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                        Text(
                            "${entry.type.label()} · any day",
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                        )
                    }
                }
                Text(
                    title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = if (recipe != null) Modifier.clickable(onClick = onOpenRecipe) else Modifier,
                )
                if (recipe == null && entry.text.isNotBlank()) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Icon(Icons.Rounded.Notes, null, tint = scheme.primary, modifier = Modifier.size(16.dp))
                        Text(
                            entry.text,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                // On a day card the stepper sits under the title; on an any-day card it joins the Cook row.
                val servings = entry.servings
                if (recipe != null && servings != null && cookLabel == null) {
                    ServingsStepper(servings, canEdit, title, onServings)
                }
            }
            if (canEdit) {
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "Options for ${title}")
                    }
                    DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Rounded.Edit, null) },
                            onClick = { menuOpen = false; onEdit() },
                        )
                        if (recipe != null) {
                            DropdownMenuItem(
                                text = { Text("Add to shopping list") },
                                leadingIcon = { Icon(Icons.Rounded.AddShoppingCart, null) },
                                onClick = { menuOpen = false; onAddToList() },
                            )
                        }
                        HorizontalDividerInMenu()
                        DropdownMenuItem(
                            text = { Text("Remove from meal plan") },
                            leadingIcon = { Icon(Icons.Outlined.EventBusy, null) },
                            onClick = { menuOpen = false; onRemove() },
                        )
                    }
                }
            }
        }
        if (cookLabel != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(end = 4.dp),
            ) {
                if (canEdit) {
                    FilledTonalButton(onClick = onCook) {
                        Icon(Icons.Rounded.Restaurant, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.size(8.dp))
                        Text(cookLabel)
                    }
                }
                Spacer(Modifier.weight(1f))
                val servings = entry.servings
                if (recipe != null && servings != null) ServingsStepper(servings, canEdit, title, onServings)
            }
        }
    }
}

@Composable
private fun HorizontalDividerInMenu() {
    androidx.compose.material3.HorizontalDivider(
        modifier = Modifier.padding(vertical = 4.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun PlaceholderThumb(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(28.dp))
    }
}

/** Servings stepper; read-only text while offline. */
@Composable
private fun ServingsStepper(
    servings: Double,
    canEdit: Boolean,
    title: String,
    onServings: (Int) -> Unit,
) {
    if (canEdit) {
        StepperPill(
            label = servingsLabel(servings),
            onDecrease = { onServings(-1) },
            onIncrease = { onServings(1) },
            decreaseDescription = "Fewer servings of ${title}",
            increaseDescription = "More servings of ${title}",
            canDecrease = servings > 1.0,
            size = StepperPillSize.Compact,
            labelMinWidth = 76.dp,
        )
    } else {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Icon(Icons.Rounded.Group, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
            Text(
                servingsLabel(servings),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
