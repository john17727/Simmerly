package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import coil3.compose.AsyncImage
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.core.presentation.shimmer
import dev.juanrincon.simmerly.recipes.domain.model.Note
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.LocalRecipesTopBarController
import dev.juanrincon.simmerly.recipes.presentation.details.models.IngredientUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.InstructionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.NutritionUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore
import dev.juanrincon.simmerly.recipes.presentation.shared.RecipeMetaRow
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun RecipeDetailsScreen(
    recipeId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: RecipeDetailsViewModel = koinViewModel { parametersOf(recipeId) },
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    Content(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (state.showSettings) {
        ModalBottomSheet(
            onDismissRequest = {
                onEvent(RecipeDetailsStore.Intent.DismissSettings)
            },
        ) {
            SettingsView(state.recipe.settings)
        }
    }

    val controller = LocalRecipesTopBarController.current
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    val isExpanded =
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    SideEffect {
        if (isExpanded) {
            controller.title = { Text("Recipes") }
            controller.navigationIcon = {}
        } else {
            controller.title = { Text(state.recipe.title) }
            controller.navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
        }
        controller.actions = {
            IconButton(onClick = { onEvent(RecipeDetailsStore.Intent.ShowSettings) }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { controller.reset() }
    }

    AnimatedContent(state.mode) { mode ->
        when (mode) {
            RecipeDetailsStore.RecipeMode.READ_ONLY -> {
                AnimatedContent(isExpanded) { expanded ->
                    if (expanded) {
                        ExpandedView(
                            state = state,
                            onEvent = onEvent,
                            modifier = modifier
                        )
                    } else {
                        CompactView(
                            state = state,
                            onEvent = onEvent,
                            modifier = modifier
                        )
                    }
                }
            }

            RecipeDetailsStore.RecipeMode.EDIT -> TODO()
        }
    }
}

@Composable
private fun CompactView(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val recipe = state.recipe
    val tabs = remember(recipe.notes.isNotEmpty(), recipe.settings.showNutrition) {
        buildList {
            add("Ingredients")
            add("Instructions")
            if (recipe.notes.isNotEmpty()) add("Notes")
            if (recipe.settings.showNutrition) add("Nutrition")
        }
    }

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Content sections start at index 3 (image=0, meta=1, stickyHeader=2, sections=3+)
    val selectedTabIndex by remember {
        derivedStateOf {
            (listState.firstVisibleItemIndex - 3).coerceIn(0, tabs.lastIndex)
        }
    }

    LazyColumn(state = listState, modifier = modifier) {

        // Hero image
        item {
            AsyncImage(
                model = recipe.image,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillParentMaxHeight(0.33f)
                    .clip(MaterialTheme.shapes.medium)
            )
        }

        // Description + meta
        item {
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Spacer(modifier = Modifier.height(16.dp))
                if (recipe.description.isNotBlank()) {
                    Text(
                        recipe.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                RecipeMetaRow(
                    recipe.rating,
                    recipe.totalTime,
                    recipe.prepTime,
                    recipe.performTime
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Sticky tab row
        stickyHeader {
            PrimaryScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            coroutineScope.launch {
                                listState.animateScrollToItem(index + 3)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }
        }

        // Ingredients & Tools (always shown, index 3)
        item {
            IngredientAndToolView(
                recipe = recipe,
                onRemoveServingButtonClick = { onEvent(RecipeDetailsStore.Intent.RemoveServing) },
                onAddServingButtonClick = { onEvent(RecipeDetailsStore.Intent.AddServing) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .ifTrue(state.loading) {
                        height(400.dp).shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
            )
        }

        // Instructions (always shown, index 4)
        item {
            InstructionView(
                instructions = recipe.instructions,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    )
                    .ifTrue(state.loading) {
                        height(600.dp).shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
            )
        }

        // Notes (conditional)
        if (recipe.notes.isNotEmpty()) {
            item {
                NotesView(
                    recipe.notes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                )
            }
        }

        // Nutrition (conditional)
        if (recipe.settings.showNutrition) {
            item {
                NutritionView(
                    recipe.nutrition,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .wrapContentHeight(unbounded = true)
                        .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ExpandedView(
    state: RecipeDetailsStore.State,
    onEvent: (RecipeDetailsStore.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(EXPANDED_CARD_PADDING)
    ) {
        Column(
            modifier = Modifier
                .widthIn(200.dp, 300.dp)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IngredientAndToolView(
                recipe = state.recipe,
                onRemoveServingButtonClick = { onEvent(RecipeDetailsStore.Intent.RemoveServing) },
                onAddServingButtonClick = { onEvent(RecipeDetailsStore.Intent.AddServing) },
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                    .ifTrue(state.loading) {
                        height(600.dp).shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
            )
            AnimatedVisibility(state.recipe.settings.showNutrition) {
                NutritionView(
                    state.recipe.nutrition, modifier = Modifier.background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = MaterialTheme.shapes.medium
                    ).wrapContentHeight(unbounded = true) // allow shrinking to content height
                        .padding(top = 32.dp, bottom = 16.dp, start = 16.dp, end = 16.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            InstructionView(
                instructions = state.recipe.instructions,
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = MaterialTheme.shapes.medium
                )
                    .ifTrue(state.loading) {
                        height(800.dp).shimmer(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceContainer,
                                MaterialTheme.colorScheme.surfaceContainerHighest,
                                MaterialTheme.colorScheme.surfaceContainer,
                            ),
                            shape = MaterialTheme.shapes.medium
                        )
                    }
            )
            if (state.recipe.notes.isNotEmpty()) {
                NotesView(
                    state.recipe.notes,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NotesView(notes: List<Note>, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("Notes", style = MaterialTheme.typography.headlineSmall)
        }
        notes.forEach { note ->
            NoteEntry(note, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
fun NoteEntry(note: Note, modifier: Modifier = Modifier) {
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
fun NutritionView(nutrition: NutritionUi, modifier: Modifier = Modifier) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
    ) {
        Text("Nutrition", style = MaterialTheme.typography.headlineSmall)
        nutrition.calories?.let {
            NutritionEntry("Calories", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.carbohydrateContent?.let {
            NutritionEntry("Carbohydrates", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.cholesterolContent?.let {
            NutritionEntry("Cholesterol", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.fatContent?.let {
            NutritionEntry("Fat", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.fiberContent?.let {
            NutritionEntry("Fiber", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.proteinContent?.let {
            NutritionEntry("Protein", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.saturatedFatContent?.let {
            NutritionEntry("Saturated Fat", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.sodiumContent?.let {
            NutritionEntry("Sodium", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.sugarContent?.let {
            NutritionEntry("Sugar", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.transFatContent?.let {
            NutritionEntry("Trans Fat", it, modifier = Modifier.fillMaxWidth())
        }
        nutrition.unsaturatedFatContent?.let {
            NutritionEntry("Unsaturated Fat", it, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun NutritionEntry(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.SpaceBetween) {
        Text(title)
        Text(value, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun IngredientAndToolView(
    recipe: RecipeDetailUi,
    onAddServingButtonClick: () -> Unit,
    onRemoveServingButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("Ingredients", style = MaterialTheme.typography.headlineSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(recipe.formattedServings, color = MaterialTheme.colorScheme.primary)
                if (recipe.isParsed) {
                    Row {
                        IconButton(
                            onClick = onRemoveServingButtonClick,
                            enabled = recipe.servings > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = null)
                        }
                        IconButton(onClick = onAddServingButtonClick) {
                            Icon(Icons.Default.Add, contentDescription = null)
                        }
                    }
                }
            }
        }
        recipe.ingredients.forEach { ingredient ->
            IngredientEntry(ingredient, modifier = Modifier.fillMaxWidth())
        }
        if (recipe.tools.isNotEmpty()) {
            Text(
                "Tools",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 16.dp)
            )
            recipe.tools.forEach { tool ->
                Text(tool.name, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun InstructionView(
    instructions: List<InstructionUi>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 8.dp)
        ) {
            Text("Instructions", style = MaterialTheme.typography.headlineSmall)
        }
        instructions.forEach { instruction ->
            InstructionEntry(instruction, modifier = Modifier.fillMaxWidth())
        }
    }
}

@Composable
private fun IngredientEntry(ingredient: IngredientUi, modifier: Modifier = Modifier) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.Top
        ) {
            ingredient.formattedQuantity?.let {
                Text(it, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Text(ingredient.formattedDisplay)
        }
        ingredient.note?.let { note ->
            Text(
                note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

@Composable
private fun InstructionEntry(instruction: InstructionUi, modifier: Modifier = Modifier) {
    val richTextState = rememberRichTextState()
    LaunchedEffect(instruction.text) {
        richTextState.setMarkdown(instruction.text)
    }
    Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = modifier) {
        Text(
            instruction.summary,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Row {
            instruction.associatedIngredients.forEach {
                Text(it.formattedDisplay, style = MaterialTheme.typography.bodySmall)
            }
        }
        RichText(
            state = richTextState,
//        imageLoader = Coil3ImageLoader, // TODO: Add image loader
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun SettingsView(settings: Settings, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(16.dp)) {
        Text("Recipe Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(checked = settings.public, onCheckedChange = {})
            Text("Public Recipe")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(checked = settings.showNutrition, onCheckedChange = {})
            Text("Show Nutrition Values")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(checked = settings.disableComments, onCheckedChange = {})
            Text("Disable Comments")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(SETTINGS_OPTION_SPACING)
        ) {
            Switch(checked = settings.locked, onCheckedChange = {})
            Text("Locked")
        }
    }
}

val EXPANDED_CARD_PADDING = 16.dp
val SETTINGS_OPTION_SPACING = 16.dp


@Preview
@Composable
fun ExpandedViewPreview() {
//    ExpandedView(state = RecipeDetail(), modifier = Modifier.fillMaxSize())
}
