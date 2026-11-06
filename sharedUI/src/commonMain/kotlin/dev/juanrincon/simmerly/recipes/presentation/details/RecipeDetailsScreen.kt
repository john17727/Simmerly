package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.juanrincon.simmerly.core.presentation.asString
import dev.juanrincon.simmerly.core.presentation.asStringSuspend
import dev.juanrincon.simmerly.core.presentation.navigation.isMediumWidthOrWider
import dev.juanrincon.simmerly.core.presentation.navigation.isTwoPaneWidth
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsSideEffect
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeMode
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import org.orbitmvi.orbit.compose.collectAsState
import org.orbitmvi.orbit.compose.collectSideEffect

@Composable
fun RecipeDetailsScreen(
    recipeId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToComments: (recipeId: String) -> Unit,
    commentsOpen: Boolean = false,
    onStartCooking: (recipeId: String) -> Unit = {},
    onNavigateToShoppingList: (listId: String) -> Unit = {},
    sharedTransitionScope: SharedTransitionScope? = null,
    viewModel: RecipeDetailsViewModel = koinViewModel { parametersOf(recipeId) },
    modifier: Modifier = Modifier
) {
    val state by viewModel.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    viewModel.collectSideEffect { effect ->
        when (effect) {
            is RecipeDetailsSideEffect.ShowMessage ->
                snackbarHostState.showSnackbar(effect.message.asStringSuspend())

            is RecipeDetailsSideEffect.ItemsAdded -> {
                val result = snackbarHostState.showSnackbar(
                    message = effect.message.asStringSuspend(),
                    actionLabel = "View list",
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) onNavigateToShoppingList(effect.listId)
            }
        }
    }
    Content(
        state = state,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack,
        onNavigateToComments = onNavigateToComments,
        commentsOpen = commentsOpen,
        onStartCooking = onStartCooking,
        sharedTransitionScope = sharedTransitionScope,
        snackbarHostState = snackbarHostState,
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
    commentsOpen: Boolean,
    onStartCooking: (recipeId: String) -> Unit,
    snackbarHostState: SnackbarHostState,
    sharedTransitionScope: SharedTransitionScope? = null,
    modifier: Modifier = Modifier
) {
    state.addToList?.let { sheet ->
        // Desktop gets the single-step dialog from the Simmerly Desktop design; narrower windows
        // keep the phone's three-step sheet.
        if (isTwoPaneWidth()) {
            AddToListDialog(
                sheet = sheet,
                recipeTitle = state.recipe.title.asString(),
                ingredients = state.recipe.ingredients,
                onEvent = onEvent,
            )
        } else {
            AddToListSheet(
                sheet = sheet,
                recipeTitle = state.recipe.title.asString(),
                ingredients = state.recipe.ingredients,
                onEvent = onEvent,
            )
        }
    }

    // Phones get a modal side sheet; foldable and desktop widths dock a standard one beside the
    // recipe (below, around the content).
    val sideBySide = isMediumWidthOrWider()
    if (state.showSettings && !sideBySide) {
        ModalSettingsSideSheet(
            settings = state.recipe.settings,
            onSettingChanged = { onEvent(RecipeDetailsIntent.UpdateSettings(it)) },
            onDismiss = { onEvent(RecipeDetailsIntent.DismissSettings) },
        )
    }

    val isExpanded = isTwoPaneWidth()
    // The docked settings sheet narrows the recipe to about phone width, so it gets the phone layout.
    val settingsDocked = state.showSettings && sideBySide

    Row(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            AnimatedContent(state.mode) { mode ->
                when (mode) {
                    RecipeMode.READ_ONLY -> {
                        AnimatedContent(isExpanded) { expanded ->
                            if (expanded) {
                                ExpandedView(
                                    state = state,
                                    onEvent = onEvent,
                                    onNavigateToComments = onNavigateToComments,
                                    commentsOpen = commentsOpen,
                                    settingsOpen = settingsDocked,
                                    onStartCooking = onStartCooking,
                                    modifier = modifier,
                                    snackbarHostState = snackbarHostState,
                                )
                            } else {
                                CompactRecipeDetails(
                                    state = state,
                                    onEvent = onEvent,
                                    onNavigateBack = onNavigateBack,
                                    onNavigateToComments = onNavigateToComments,
                                    onStartCooking = onStartCooking,
                                    commentsOpen = commentsOpen,
                                phoneLayout = settingsDocked || commentsOpen,
                                    sharedTransitionScope = sharedTransitionScope,
                                    modifier = modifier,
                                    snackbarHostState = snackbarHostState,
                                )
                            }
                        }
                    }

                    RecipeMode.EDIT -> TODO()
                }
            }
        }
        StandardSettingsSideSheet(
            visible = state.showSettings && sideBySide,
            settings = state.recipe.settings,
            onSettingChanged = { onEvent(RecipeDetailsIntent.UpdateSettings(it)) },
            onClose = { onEvent(RecipeDetailsIntent.DismissSettings) },
        )
    }
}
