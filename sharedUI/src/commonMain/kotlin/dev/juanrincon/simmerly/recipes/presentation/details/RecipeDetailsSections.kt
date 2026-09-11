package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.recipes.domain.model.Note
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.entries
import dev.juanrincon.simmerly.recipes.presentation.shared.IngredientChipRow
import dev.juanrincon.simmerly.theme.Simmerly

/** Inner padding shared by the sections that sit inside a card on every layout. */
private val SECTION_PADDING = 20.dp

/**
 * Width of the quantity column in an ingredient row. Fixed rather than intrinsic so every
 * ingredient name starts on the same vertical line, as the design draws it — a ragged left edge is
 * what a wrap-content quantity column would give on a recipe mixing "24" with "1/2 bunch".
 *
 * Wider than the design's 78px: that artboard used abbreviated units ("1 tbsp"), but Mealie spells
 * them out when a unit has useAbbreviation off, and "2 tablespoons" has to fit on one line.
 */
private val INGREDIENT_QUANTITY_WIDTH = 104.dp

@Composable
internal fun IngredientAndToolView(
    recipe: RecipeDetailUi,
    onAddServingButtonClick: () -> Unit,
    onRemoveServingButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(SECTION_PADDING)) {
        Text("Ingredients", style = MaterialTheme.typography.headlineSmall)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                recipe.formattedServings,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (recipe.isParsed) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    ServingStepperButton(
                        icon = Icons.Default.Remove,
                        contentDescription = "Fewer servings",
                        onClick = onRemoveServingButtonClick,
                        enabled = recipe.servings > 1
                    )
                    ServingStepperButton(
                        icon = Icons.Default.Add,
                        contentDescription = "More servings",
                        onClick = onAddServingButtonClick
                    )
                }
            }
        }
        Column(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            recipe.ingredients.forEachIndexed { index, ingredient ->
                IngredientEntry(ingredient, modifier = Modifier.fillMaxWidth(), index != 0)
            }
        }
        if (recipe.tools.isNotEmpty()) {
            Text(
                "Tools",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
            )
            recipe.tools.forEach { tool ->
                Text(tool.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
        OutlinedButton(
            onClick = { /* TODO: add to shopping list */ },
            shape = CircleShape,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(40.dp)
        ) {
            Icon(
                Icons.AutoMirrored.Default.ListAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                "Add to shopping list",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

@Composable
private fun ServingStepperButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedIconButton(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        modifier = modifier.size(32.dp)
    ) {
        Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun IngredientEntry(
    ingredient: IngredientUi,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = true
) {
    Column(modifier = modifier) {
        if (hasDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 9.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = ingredient.formattedQuantity.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.width(INGREDIENT_QUANTITY_WIDTH)
            )
            Column {
                Text(ingredient.formattedDisplay, style = MaterialTheme.typography.bodyMedium)
                ingredient.note?.let { note ->
                    Text(
                        note,
                        style = MaterialTheme.typography.bodySmall,
                        color = Simmerly.Neutral500
                    )
                }
            }
        }
    }
}

@Composable
internal fun InstructionView(
    instructions: List<InstructionUi>,
    ingredients: List<IngredientUi>,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(SECTION_PADDING)
) {
    val ingredientsById = remember(ingredients) { ingredients.associateBy { it.referenceId } }
    Column(modifier = modifier.padding(contentPadding)) {
        Text("Instructions", style = MaterialTheme.typography.headlineSmall)
        instructions.forEachIndexed { index, instruction ->
            InstructionEntry(
                stepNumber = index + 1,
                instruction = instruction,
                ingredients = instruction.ingredientIds.mapNotNull { ingredientsById[it] },
                // Only the first step clears the heading; the rest butt up against the divider
                // that closes the step above them.
                modifier = Modifier.fillMaxWidth().ifTrue(index == 0) { padding(top = 12.dp) },
                hasDivider = index != 0
            )
        }
    }
}

@Composable
private fun InstructionEntry(
    stepNumber: Int,
    instruction: InstructionUi,
    ingredients: List<IngredientUi>,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = true
) {
    val richTextState = rememberRichTextState()
    LaunchedEffect(instruction.text) {
        richTextState.setMarkdown(instruction.text)
    }
    Column(modifier = modifier) {
        if (hasDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
            ) {
                Text(
                    stepNumber.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Skip the generated "Step N" heading — the badge to the left already says it.
                if (instruction.hasOwnSummary && instruction.summary.isNotBlank()) {
                    Text(
                        instruction.summary,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                IngredientChipRow(ingredients, modifier = Modifier.fillMaxWidth())
                // RichText picks its style up from LocalTextStyle; providing it that way keeps
                // us off the experimental `style` parameter while still applying DM Sans and the
                // design's 16/27 step metrics.
                ProvideTextStyle(
                    MaterialTheme.typography.bodyLarge.copy(lineHeight = 27.sp)
                ) {
                    RichText(
                        state = richTextState,
                        modifier = Modifier.widthIn(max = INSTRUCTION_TEXT_MAX_WIDTH)
                    )
                }
                instruction.images.forEach { image ->
                    AsyncImage(
                        model = image,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .widthIn(max = INSTRUCTION_IMAGE_MAX_WIDTH)
                            .height(INSTRUCTION_IMAGE_HEIGHT)
                            .clip(MaterialTheme.shapes.medium)
                    )
                }
            }
        }
    }
}

// Measure caps from the design. Instruction prose is capped so a wide desktop pane keeps a
// readable line length instead of stretching to the full column, and step photos are cropped to a
// consistent band rather than each taking whatever height its aspect ratio implies.
private val INSTRUCTION_TEXT_MAX_WIDTH = 640.dp
private val INSTRUCTION_IMAGE_MAX_WIDTH = 560.dp
private val INSTRUCTION_IMAGE_HEIGHT = 260.dp

@Composable
internal fun NotesView(notes: List<Note>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(SECTION_PADDING),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Notes", style = MaterialTheme.typography.headlineSmall)
        notes.forEach { note ->
            NoteEntry(note, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NoteEntry(note: Note, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (note.title.isNotEmpty()) {
            Text(
                note.title, style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Text(note.text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
internal fun NutritionView(nutrition: NutritionUi, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(SECTION_PADDING)) {
        Text(
            "Nutrition",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        nutrition.entries.forEachIndexed { index, entry ->
            NutritionEntry(entry.label, entry.value, modifier = Modifier.fillMaxWidth(), index != 0)
        }
    }
}

@Composable
private fun NutritionEntry(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    hasDivider: Boolean = true
) {
    Column(modifier = modifier) {
        if (hasDivider) {
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceContainer)
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, style = MaterialTheme.typography.bodySmall)
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
