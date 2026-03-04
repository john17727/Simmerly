package dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin

import app.tracktion.core.domain.util.Result
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.RecipesError
import dev.juanrincon.simmerly.recipes.domain.model.RecipeDetail
import dev.juanrincon.simmerly.recipes.presentation.details.mappers.toRecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.models.RecipeDetailUi
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStoreFactory.Msg.RecipeUpdated
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
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
        data object Loading : Msg
        data class RecipeUpdated(val recipe: RecipeDetailUi) : Msg
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
        }

        override fun executeAction(action: Action) {
            when (action) {
                is Action.LoadRecipe -> observeRecipe(action.recipeId)
            }
        }

        private fun observeRecipe(recipeId: String) {
            repository.recipeDetails(recipeId)
                .distinctUntilChanged()
                .onEach { result ->
                    when (result) {
                        is Result.Error<RecipesError> -> TODO()
                        is Result.Success<LoadingResult<RecipeDetail>> -> {
                            when (result.data) {
                                is LoadingResult.Loaded<RecipeDetail> -> dispatch(
                                    RecipeUpdated(
                                        result.data.data.toRecipeDetailUi()
                                    )
                                )

                                LoadingResult.Loading -> dispatch(Msg.Loading)
                            }
                        }
                    }
                }.launchIn(scope)
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
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is RecipeUpdated -> copy(loading = false, recipe = msg.recipe)
                is Msg.Loading -> copy(loading = true)
                Msg.ShowSettings -> copy(showSettings = true)
                Msg.DismissSettings -> copy(showSettings = false)
            }
    }
}