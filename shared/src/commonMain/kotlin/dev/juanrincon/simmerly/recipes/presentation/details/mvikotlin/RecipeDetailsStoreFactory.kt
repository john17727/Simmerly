package dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin

import arrow.core.Either
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.domain.model.Settings
import dev.juanrincon.simmerly.recipes.presentation.details.mappers.toRecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStoreFactory.Msg.RecipeUpdated
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.math.round

class RecipeDetailsStoreFactory(
    private val recipeId: String,
    private val storeFactory: StoreFactory,
    private val repository: RecipeRepository
) {

    fun create(): RecipeDetailsStore =
        object : RecipeDetailsStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RecipeDetailsStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(recipeId),
            executorFactory = { ExecutorImpl(repository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class LoadRecipe(val recipeId: String) : Action
    }

    private sealed interface Msg {
        data class Loading(val isLoading: Boolean) : Msg
        data class Refreshing(val isRefreshing: Boolean) : Msg
        data class RecipeUpdated(val recipe: RecipeDetailUi) : Msg
        data class Error(val error: RecipesError) : Msg
        data class SettingsUpdated(val settings: Settings) : Msg
        data object ShowSettings : Msg
        data object DismissSettings : Msg
    }

    private class BootstrapperImpl(val recipeId: String) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadRecipe(recipeId))
        }
    }

    private class ExecutorImpl(
        private val repository: RecipeRepository
    ) : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) = when (intent) {
            Intent.AddServing -> updateServing(state().recipe.servings + 1)
            Intent.RemoveServing -> updateServing(state().recipe.servings - 1)
            Intent.ShowSettings -> dispatch(Msg.ShowSettings)
            Intent.DismissSettings -> dispatch(Msg.DismissSettings)
            is Intent.UpdateSettings -> updateSettings(intent.settings)
        }

        override fun executeAction(action: Action) {
            when (action) {
                is Action.LoadRecipe -> observeRecipe(action.recipeId)
            }
        }

        private fun observeRecipe(recipeId: String) {
            repository.recipeDetails(recipeId)
                .distinctUntilChanged()
                .onEach(::handleRecipeUpdates).launchIn(scope)
        }

        private fun updateSettings(settings: Settings) {
            val recipeId = state().recipe.id
            // Optimistic update — reflect the toggle immediately in the UI
            dispatch(Msg.SettingsUpdated(settings))
            scope.launch {
                repository.updateSettings(recipeId, settings)
                // On error you could revert by dispatching the old settings;
                // for now we let the next recipeDetails emission reconcile the state.
            }
        }

        private fun updateServing(newServings: Double) {
            val current = state().recipe
            if (current == RecipeDetailUi.emptyRecipe) return

            // Avoid 0 or negative servings; adjust bounds as needed
            val clamped = newServings.coerceAtLeast(1.0)
            if (clamped == current.servings) return

            val factor = clamped / current.servings

            val updatedIngredients = current.ingredients.map { ingredient ->
                if (ingredient.quantity == null) return@map ingredient
                // If your IngredientUi quantity can be null, guard it here
                val newQty = (ingredient.quantity * factor)
                // Optional: round to sensible precision for display
                val rounded = (round(newQty * 100.0) / 100.0)
                ingredient.copy(quantity = rounded)
            }

            val updatedRecipe = current.copy(
                servings = clamped,
                ingredients = updatedIngredients
            )

            dispatch(RecipeUpdated(updatedRecipe))
        }

        private suspend fun handleRecipeUpdates(response: Either<RecipesError, LoadingResult<RecipeDetail>>) =
            response.fold(
                ifLeft = { error ->
                    if (state().recipe == RecipeDetailUi.emptyRecipe) {
                        dispatch(Msg.Error(error))
                    } else {
                        dispatch(Msg.Refreshing(false))
                    }
                },
                ifRight = { result ->
                    when (result) {
                        LoadingResult.Loading -> dispatch(Msg.Loading(true))
                        LoadingResult.Refreshing -> dispatch(Msg.Refreshing(true))
                        LoadingResult.RefreshComplete -> dispatch(Msg.Refreshing(false))
                        is LoadingResult.Loaded -> dispatch(RecipeUpdated(result.data.toRecipeDetailUi()))
                    }
                }
            )
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is RecipeUpdated -> copy(
                    loading = false,
                    isRefreshing = false,
                    error = null,
                    recipe = msg.recipe,
                    mobileTabs = buildList {
                        add("Overview")
                        add("Ingredients")
                        add("Instructions")
                        if (msg.recipe.notes.isNotEmpty()) add("Notes")
                        if (msg.recipe.settings.showNutrition) add("Nutrition")
                    },
                    desktopTabs = buildList {
                        add("Recipe")
                        if (msg.recipe.notes.isNotEmpty()) add("Notes")
                        if (!msg.recipe.settings.disableComments) add("Comments")
                    }
                )

                is Msg.Refreshing -> copy(isRefreshing = msg.isRefreshing)
                is Msg.Error -> copy(loading = false, isRefreshing = false, error = msg.error)
                is Msg.SettingsUpdated -> copy(recipe = recipe.copy(settings = msg.settings))
                is Msg.Loading -> copy(loading = msg.isLoading)
                Msg.ShowSettings -> copy(showSettings = true)
                Msg.DismissSettings -> copy(showSettings = false)
            }
    }
}