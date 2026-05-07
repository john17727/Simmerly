package dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin

import app.tracktion.core.domain.util.Result
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.recipes.domain.LoadingResult
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStore.Label
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStore.State
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStoreFactory.Msg.Loading
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStoreFactory.Msg.QueryChanged
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStoreFactory.Msg.QuerySubmitted
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStoreFactory.Msg.RecipesLoaded
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class RecipeSearchStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RecipeRepository
) {

    fun create(): RecipeSearchStore =
        object : RecipeSearchStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RecipeSearchStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl(repository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data object LoadRecipes : Action
    }

    private sealed interface Msg {
        data class Loading(val isLoading: Boolean) : Msg
        data class RecipesLoaded(val recipes: List<RecipeSummary>) : Msg
        data class QueryChanged(val query: String) : Msg
        data class QuerySubmitted(val query: String) : Msg
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadRecipes)
        }
    }

    private class ExecutorImpl(private val repository: RecipeRepository) :
        CoroutineExecutor<Intent, Action, State, Msg, Label>() {

        override fun executeIntent(intent: Intent) {
            when (intent) {
                is Intent.OnQueryChanged -> dispatch(QueryChanged(intent.query))
                is Intent.OnQuerySubmitted -> dispatch(QuerySubmitted(intent.query))
            }
        }

        override fun executeAction(action: Action) = when (action) {
            Action.LoadRecipes -> fetchRecipes()
        }

        private fun fetchRecipes() {
            repository.recipeList(page = 1, refresh = false)
                .onEach { result ->
                    when (result) {
                        is Result.Success -> when (val lr = result.data) {
                            is LoadingResult.Loading -> dispatch(Loading(true))
                            is LoadingResult.Loaded -> dispatch(RecipesLoaded(lr.data.items))
                        }

                        is Result.Error -> dispatch(Loading(false))
                    }
                }
                .launchIn(scope)
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is Loading -> copy(isLoading = msg.isLoading)
                is RecipesLoaded -> copy(recipes = msg.recipes, isLoading = false)
                is QueryChanged -> copy(searchQuery = msg.query)
                is QuerySubmitted -> copy(submittedQuery = msg.query)
            }
    }
}
