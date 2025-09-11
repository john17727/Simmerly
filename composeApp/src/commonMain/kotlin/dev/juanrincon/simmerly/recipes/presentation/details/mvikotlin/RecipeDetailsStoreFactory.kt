package dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin

import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State

internal class RecipeDetailsStoreFactory(
    private val recipeId: String,
    private val storeFactory: StoreFactory,
    private val repository: RecipeRepository
) {

    fun create(): RecipeDetailsStore =
        object : RecipeDetailsStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RecipeDetailsStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl(recipeId, repository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data object LoadRecipe : Action
    }

    private sealed interface Msg {
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
        }
    }

    private class ExecutorImpl(
        private val recipeId: String,
        private val repository: RecipeRepository
    ) : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) {
            TODO()
        }

        override fun executeAction(action: Action) {
            when (action) {
                Action.LoadRecipe -> {
//                    val recipe = repository.getRecipe(recipeId)
//                    dispatch(Msg.RecipeLoaded(recipe))
                }
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                else -> copy()
            }
    }
}