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
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStoreFactory.Msg.RecipeUpdated
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

internal class RecipeDetailsStoreFactory(
    private val recipeId: String,
    private val storeFactory: StoreFactory,
    private val repository: RecipeRepository
) {

    fun create(): RecipeDetailsStore =
        object : RecipeDetailsStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RecipeDetailsStore",
            initialState = State.Loading,
            bootstrapper = BootstrapperImpl(recipeId),
            executorFactory = { ExecutorImpl(repository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data class LoadRecipe(val recipeId: String) : Action
    }

    private sealed interface Msg {
        data object Loading : Msg
        data class RecipeUpdated(val recipe: RecipeDetail) : Msg
    }

    private class BootstrapperImpl(val recipeId: String) : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadRecipe(recipeId))
        }
    }

    private class ExecutorImpl(
        private val repository: RecipeRepository
    ) : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) {
            TODO()
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
                                        result.data.data
                                    )
                                )

                                LoadingResult.Loading -> dispatch(Msg.Loading)
                            }
                        }
                    }
                }.launchIn(scope)
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is RecipeUpdated -> State.Content(msg.recipe)
                is Msg.Loading -> State.Loading
            }
    }
}