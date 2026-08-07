package dev.juanrincon.simmerly.recipes.presentation.cookmode

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.UiText
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.domain.model.Tool
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeIntent
import dev.juanrincon.simmerly.recipes.presentation.cookmode.orbit.CookModeState
import dev.juanrincon.simmerly.recipes.presentation.details.models.FoodUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.UnitUi
import dev.juanrincon.simmerly.recipes.presentation.shared.TagChip
import dev.juanrincon.simmerly.theme.SimmerlyTheme

/**
 * The "mise en place" step (design frame 02): every ingredient gets checked off, and tools are
 * called out, before a cook moves into the timed steps. "Add missing" is drawn per the design but
 * intentionally inert — there is no shopping-list feature in the app for it to add to.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MiseEnPlaceView(
    state: CookModeState,
    onEvent: (CookModeIntent) -> Unit,
    onExit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val readyCount = state.readyIngredientCount
    val totalCount = recipe.ingredients.size
    val progress = if (totalCount == 0) 0f else readyCount / totalCount.toFloat()
    val allIngredientsChecked = totalCount == 0 || readyCount == totalCount
    val canStartCooking = recipe.instructions.isNotEmpty() && allIngredientsChecked

    Scaffold(
        modifier = modifier.fillMaxSize(),
        // The sheet hosting this screen draws edge-to-edge with its own insets zeroed out (see
        // BottomSheetSceneStrategy), so topBar/bottomBar below pad themselves against the status
        // and navigation bars directly instead of relying on Scaffold's automatic accounting.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            Column(modifier = Modifier.statusBarsPadding()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(64.dp).padding(horizontal = 4.dp)
                ) {
                    IconButton(onClick = onExit) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                    Text(
                        "Mise en place",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { onEvent(CookModeIntent.SkipMiseEnPlace) }) {
                        Text("Skip")
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().padding(start = 24.dp, end = 24.dp, bottom = 4.dp)
                ) {
                    Text(
                        recipe.title.asString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    // The stepper only makes sense when at least one ingredient has a parsed
                    // quantity to scale — same gate RecipeDetailsSections.kt uses for the read-only
                    // recipe screen's identical stepper.
                    if (recipe.isParsed) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            ServingStepperButton(
                                icon = Icons.Default.Remove,
                                contentDescription = "Remove serving",
                                enabled = recipe.servings > 1,
                                onClick = { onEvent(CookModeIntent.RemoveServing) }
                            )
                            Text(
                                recipe.formattedServings,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.widthIn(min = 76.dp)
                            )
                            ServingStepperButton(
                                icon = Icons.Default.Add,
                                contentDescription = "Add serving",
                                enabled = true,
                                onClick = { onEvent(CookModeIntent.AddServing) }
                            )
                        }
                    } else {
                        Text(
                            recipe.formattedServings,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 8.dp)
                ) {
                    Text(
                        "$readyCount OF $totalCount READY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.ShoppingBasket,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            "Add missing",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainer,
                    // M3's default stop indicator draws a dot at the track's end regardless of
                    // progress, which reads as a stray mark at 0 of N ready.
                    drawStopIndicator = {}
                )
                Spacer(Modifier.height(8.dp))
            }
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .navigationBarsPadding()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Screen stays awake while you cook.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(
                    onClick = { onEvent(CookModeIntent.BeginSteps) },
                    enabled = canStartCooking,
                    shape = RoundedCornerShape(percent = 50),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text("Start cooking")
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            items(recipe.ingredients, key = { it.referenceId }) { ingredient ->
                CheckableIngredientRow(
                    ingredient = ingredient,
                    checked = ingredient.referenceId in state.checkedIngredientIds,
                    onToggle = { onEvent(CookModeIntent.ToggleIngredient(ingredient.referenceId)) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (recipe.tools.isNotEmpty()) {
                item { ToolsSection(recipe.tools, modifier = Modifier.fillMaxWidth()) }
            }
        }
    }
}

@Composable
private fun CheckableIngredientRow(
    ingredient: IngredientUi,
    checked: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .clickable(onClick = onToggle)
            .padding(horizontal = 24.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(22.dp)
                .then(
                    if (checked) {
                        Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                    } else {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                    }
                )
        ) {
            if (checked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Column {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                ingredient.formattedQuantity?.let {
                    Text(
                        it,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        textDecoration = if (checked) TextDecoration.LineThrough else null,
                        color = if (checked) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    )
                }
                Text(
                    ingredient.formattedDisplay,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (checked) TextDecoration.LineThrough else null,
                    color = if (checked) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
                )
            }
            ingredient.note?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ToolsSection(tools: List<Tool>, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp)) {
        Text(
            "TOOLS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 10.dp)
        )
        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tools.forEach { tool -> TagChip(name = tool.name) }
        }
    }
}

@Composable
private fun ServingStepperButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(enabled = enabled, onClick = onClick)
    ) {
        Icon(
            icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (enabled) 1f else 0.4f),
            modifier = Modifier.size(18.dp)
        )
    }
}

// region Previews

/** Shared across the Cook Mode preview composables in this package — five steps, two of which
 * carry a detectable duration, so both the mise en place, timer-less, and timer-bearing step
 * previews all have something real to render. */
internal val previewCookRecipe = RecipeDetailUi(
    id = "preview-recipe",
    title = UiText.Dynamic("Spaghetti Carbonara"),
    image = "",
    description = UiText.Dynamic(
        "A classic Roman pasta dish made with eggs, Pecorino Romano, guanciale, and black pepper."
    ),
    rating = 4.8,
    totalTime = "30 min",
    prepTime = "10 min",
    performTime = "20 min",
    servings = 2.0,
    favorite = false,
    link = null,
    tags = emptyList(),
    ingredients = listOf(
        IngredientUi(
            referenceId = "ingredient-1",
            quantity = 200.0,
            display = "200 g Spaghetti",
            // Parsed (food/unit != null) so the preview exercises the servings stepper, not the
            // static-text fallback recipe.isParsed falls back to when nothing is parsed. Still
            // renders as "200 g Spaghetti" via formattedQuantity/formattedDisplay.
            food = FoodUi(name = "spaghetti", pluralName = "spaghetti"),
            unit = UnitUi(
                name = "gram",
                pluralName = "grams",
                fraction = false,
                abbreviation = "g",
                pluralAbbreviation = "g",
                useAbbreviation = true
            ),
            note = null
        ),
        IngredientUi(
            referenceId = "ingredient-2",
            quantity = 100.0,
            display = "100 g Guanciale",
            food = null,
            unit = null,
            note = "pancetta works too"
        ),
        IngredientUi(
            referenceId = "ingredient-3",
            quantity = 2.0,
            display = "2 Large eggs",
            food = null,
            unit = null,
            note = null
        ),
        IngredientUi(
            referenceId = "ingredient-4",
            quantity = 50.0,
            display = "50 g Pecorino Romano",
            food = null,
            unit = null,
            note = null
        ),
    ),
    instructions = listOf(
        InstructionUi(
            id = "step-1",
            title = null,
            summary = "Step 1",
            // A range rather than a single duration, so the detected-range card has a preview.
            text = "Bring a large pot of salted water to a boil. Cook the spaghetti for 15–17 minutes, until al dente.",
            ingredientIds = listOf("ingredient-1")
        ),
        InstructionUi(
            id = "step-2",
            title = null,
            summary = "Step 2",
            text = "Cut the guanciale into short batons. Cook for 8 minutes, until the fat has run clear and the edges are crisp.",
            ingredientIds = listOf("ingredient-2")
        ),
        InstructionUi(
            id = "step-3",
            title = null,
            summary = "Step 3",
            text = "Whisk the eggs and grated pecorino into a thick paste. Grind in the pepper.",
            ingredientIds = listOf("ingredient-3", "ingredient-4")
        ),
    ),
    tools = listOf(
        Tool(id = "tool-1", groupId = "", name = "Large pot"),
        Tool(id = "tool-2", groupId = "", name = "Heavy skillet"),
        Tool(id = "tool-3", groupId = "", name = "Mixing bowl"),
    ),
    nutrition = NutritionUi(
        calories = "620 kcal",
        carbohydrateContent = "72g",
        cholesterolContent = "210mg",
        fatContent = "24g",
        fiberContent = "3g",
        proteinContent = "28g",
        saturatedFatContent = "9g",
        sodiumContent = "580mg",
        sugarContent = "2g",
        transFatContent = "0g",
        unsaturatedFatContent = "13g"
    ),
    notes = emptyList(),
    settings = Settings(
        public = true,
        showNutrition = false,
        showAssets = false,
        landscapeView = false,
        disableComments = false,
        locked = false
    )
)

private val previewMiseEnPlaceState = CookModeState(
    loading = false,
    recipe = previewCookRecipe,
    checkedIngredientIds = setOf("ingredient-1", "ingredient-2")
)

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun MiseEnPlaceLightPreview() {
    SimmerlyTheme {
        MiseEnPlaceView(state = previewMiseEnPlaceState, onEvent = {}, onExit = {})
    }
}

@Preview(apiLevel = 36, showSystemUi = true, device = Devices.PIXEL_9_PRO)
@Composable
private fun MiseEnPlaceDarkPreview() {
    SimmerlyTheme(darkTheme = true) {
        MiseEnPlaceView(state = previewMiseEnPlaceState, onEvent = {}, onExit = {})
    }
}

// endregion
