package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeMode
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState

@Composable
fun RecipeDetailsScreen(
    recipeId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToComments: (recipeId: String) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    viewModel: RecipeDetailsViewModel = koinViewModel { parametersOf(recipeId) },
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    Content(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToComments = onNavigateToComments,
        sharedTransitionScope = sharedTransitionScope,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Content(
    state: RecipeDetailsState,
    onEvent: (RecipeDetailsIntent) -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToComments: (recipeId: String) -> Unit,
    sharedTransitionScope: SharedTransitionScope? = null,
    modifier: Modifier = Modifier
) {
    if (state.showSettings) {
        ModalBottomSheet(
            dragHandle = null,
            onDismissRequest = { onEvent(RecipeDetailsIntent.DismissSettings) },
        ) {
            SettingsView(
                settings = state.recipe.settings,
                onSettingChanged = { onEvent(RecipeDetailsIntent.UpdateSettings(it)) },
                modifier = Modifier.padding(vertical = 16.dp)
            )
        }
    }

    val isExpanded = currentWindowAdaptiveInfoV2().windowSizeClass
        .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND)

    AnimatedContent(state.mode) { mode ->
        when (mode) {
            RecipeMode.READ_ONLY -> {
                AnimatedContent(isExpanded) { expanded ->
                    if (expanded) {
                        ExpandedView(
                            state = state,
                            onEvent = onEvent,
                            modifier = modifier
                        )
                    } else {
                        CompactRecipeDetails(
                            state = state,
                            onEvent = onEvent,
                            onNavigateBack = onNavigateBack,
                            onNavigateToComments = onNavigateToComments,
                            sharedTransitionScope = sharedTransitionScope,
                            modifier = modifier
                        )
                    }
                }
            }

            RecipeMode.EDIT -> TODO()
        }
    }
}

@Composable
private fun SettingsView(
    settings: Settings,
    onSettingChanged: (Settings) -> Unit,
    modifier: Modifier = Modifier
) {
    val spacing = 16.dp
    Column(modifier = modifier.padding(16.dp)) {
        Text("Recipe Settings", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Switch(
                checked = settings.public,
                onCheckedChange = { onSettingChanged(settings.copy(public = it)) }
            )
            Text("Public Recipe")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Switch(
                checked = settings.showNutrition,
                onCheckedChange = { onSettingChanged(settings.copy(showNutrition = it)) }
            )
            Text("Show Nutrition Values")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Switch(
                checked = settings.disableComments,
                onCheckedChange = { onSettingChanged(settings.copy(disableComments = it)) }
            )
            Text("Disable Comments")
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(spacing)
        ) {
            Switch(
                checked = settings.locked,
                onCheckedChange = { onSettingChanged(settings.copy(locked = it)) }
            )
            Text("Locked")
        }
    }
}
