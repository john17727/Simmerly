package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.lifecycle.ViewModel
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.details.mappers.toRecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.withServings
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsIntent
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsSideEffect
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeDetailsState
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.RecipeTab
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import org.orbitmvi.orbit.OrbitContainer
import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.viewmodel.orbitContainer

class RecipeDetailsViewModel(
    private val recipeId: String,
    private val repository: RecipeRepository,
) : OrbitContainerHost<RecipeDetailsState, RecipeDetailsState, RecipeDetailsSideEffect>,
    ViewModel() {

    override val container: OrbitContainer<RecipeDetailsState, RecipeDetailsState, RecipeDetailsSideEffect> =
        orbitContainer(initialState = RecipeDetailsState()) {
            observeRecipe()
        }

    // Concrete, non-generic accessors for Swift — see WelcomeViewModel for why these are needed.
    val stateFlow: StateFlow<RecipeDetailsState> get() = container.stateFlow
    val sideEffectFlow: Flow<RecipeDetailsSideEffect> get() = container.sideEffectFlow

    fun onEvent(event: RecipeDetailsIntent) {
        when (event) {
            RecipeDetailsIntent.AddServing -> updateServing(
                container.stateFlow.value.recipe.servings + 1
            )
            RecipeDetailsIntent.RemoveServing -> updateServing(
                container.stateFlow.value.recipe.servings - 1
            )
            RecipeDetailsIntent.ShowSettings -> intent {
                reduce { state.copy(showSettings = true) }
            }
            RecipeDetailsIntent.DismissSettings -> intent {
                reduce { state.copy(showSettings = false) }
            }
            is RecipeDetailsIntent.UpdateSettings -> updateSettings(event)
        }
    }

    private fun observeRecipe() = intent {
        repository.recipeDetails(recipeId)
            .distinctUntilChanged()
            .collect { response ->
                response.fold(
                    ifLeft = { error ->
                        if (state.recipe == RecipeDetailUi.emptyRecipe) {
                            reduce { state.copy(loading = false, isRefreshing = false, error = error) }
                        } else {
                            reduce { state.copy(isRefreshing = false) }
                        }
                    },
                    ifRight = { result ->
                        when (result) {
                            LoadingResult.Loading -> reduce { state.copy(loading = true) }
                            LoadingResult.Refreshing -> reduce { state.copy(isRefreshing = true) }
                            LoadingResult.RefreshComplete -> reduce { state.copy(isRefreshing = false) }
                            is LoadingResult.Loaded -> {
                                val recipe = result.data.toRecipeDetailUi()
                                reduce {
                                    state.copy(
                                        loading = false,
                                        isRefreshing = false,
                                        error = null,
                                        recipe = recipe,
                                        mobileTabs = buildList {
                                            add(RecipeTab.Overview)
                                            add(RecipeTab.Ingredients)
                                            add(RecipeTab.Instructions)
                                            if (recipe.notes.isNotEmpty()) add(RecipeTab.Notes)
                                            if (recipe.settings.showNutrition) add(RecipeTab.Nutrition)
                                        },
                                        desktopTabs = buildList {
                                            add(RecipeTab.Recipe)
                                            if (recipe.notes.isNotEmpty()) add(RecipeTab.Notes)
                                            if (!recipe.settings.disableComments) add(RecipeTab.Comments)
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
    }

    private fun updateServing(newServings: Double) = intent {
        val current = state.recipe
        if (current == RecipeDetailUi.emptyRecipe) return@intent

        val updated = current.withServings(newServings)
        if (updated == current) return@intent

        reduce { state.copy(recipe = updated) }
    }

    private fun updateSettings(event: RecipeDetailsIntent.UpdateSettings) = intent {
        // Optimistic update — reflect the toggle immediately in the UI
        reduce { state.copy(recipe = state.recipe.copy(settings = event.settings)) }
        repository.updateSettings(state.recipe.id, event.settings)
        // On error the next recipeDetails emission will reconcile the state
    }
}
