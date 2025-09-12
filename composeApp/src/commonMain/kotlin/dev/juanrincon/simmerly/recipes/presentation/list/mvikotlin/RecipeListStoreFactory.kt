package dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin

import app.tracktion.core.domain.util.fold
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore.Label
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore.Label.*
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore.State
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStoreFactory.Msg.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

internal class RecipeListStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RecipeRepository
) {

    fun create(): RecipeListStore =
        object : RecipeListStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RecipeListStore",
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
        data class SearchQueryChanged(val query: String) : Msg
        data class RecipesUpdated(val recipes: List<RecipeSummary>) : Msg
        data class PageData(val nextPage: Int?) : Msg
        data class SelectedRecipe(val recipeId: String) : Msg
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
                Intent.OnRefresh -> loadRecipes(1, refresh = true)
                is Intent.OnSearchQueryChanged -> dispatch(SearchQueryChanged(intent.query))
                Intent.OnLoadMore -> {
                    state().nextPage?.let {
                        loadRecipes(it)
                    }
                }

                is Intent.OnRecipeSelected -> dispatch(SelectedRecipe(intent.recipeId))
            }
        }

        override fun executeAction(action: Action) = when (action) {
            Action.LoadRecipes -> observeRecipes()
        }

        private fun observeRecipes() {
            repository.recipes()
                .distinctUntilChanged()
                .onEach { list ->
                    if (list.isEmpty()) {
                        loadRecipes(1)
                    } else {
                        dispatch(RecipesUpdated(list))
                    }
                }.launchIn(scope)
        }

        private fun loadRecipes(page: Int, refresh: Boolean = false) {
            scope.launch {
                repository.loadRecipes(page, refresh = refresh).fold(
                    onSuccess = {
                        dispatch(PageData(nextPage = it.next))
                    },
                    onFailure = {
                        dispatch(Loading(false))
                    }
                )
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(msg: Msg): State =
            when (msg) {
                is SearchQueryChanged -> copy(searchQuery = msg.query)
                is RecipesUpdated -> copy(recipes = msg.recipes, isLoading = false)
                is Loading -> copy(isLoading = msg.isLoading)
                is PageData -> copy(nextPage = msg.nextPage, isLoading = false)
                is SelectedRecipe -> copy(selectedRecipeId = msg.recipeId)
            }
    }
}
