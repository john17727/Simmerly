package dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin

import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.arkivanov.mvikotlin.core.store.Reducer
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.CoroutineExecutor
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.Label
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore.State
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal class RecipesStoreFactory(
    private val storeFactory: StoreFactory,
    private val repository: RecipeRepository
) {

    fun create(): RecipesStore =
        object : RecipesStore, Store<Intent, State, Label> by storeFactory.create(
            name = "RecipesStore",
            initialState = State(),
            bootstrapper = BootstrapperImpl(),
            executorFactory = { ExecutorImpl(repository) },
            reducer = ReducerImpl
        ) {}

    private sealed interface Action {
        data object LoadRecipes : Action
    }

    private sealed interface Msg {
        data class SearchQueryChanged(val query: String) : Msg
        data class RecipesFlowUpdated(val recipesFlow: Flow<PagingData<RecipeSummary>>) : Msg
    }

    private class BootstrapperImpl : CoroutineBootstrapper<Action>() {
        override fun invoke() {
            dispatch(Action.LoadRecipes)
        }
    }

    private class ExecutorImpl(private val repository: RecipeRepository) : CoroutineExecutor<Intent, Action, State, Msg, Label>() {
        override fun executeIntent(intent: Intent) = when (intent) {
            is Intent.OnRecipeClicked -> publish(Label.RecipeClicked(intent.recipeId))
            Intent.OnRefresh -> TODO()
            is Intent.OnSearchQueryChanged -> dispatch(Msg.SearchQueryChanged(intent.query))
        }

        override fun executeAction(action: Action) = when (action) {
            Action.LoadRecipes -> updateRecipes()
        }

        private fun updateRecipes() {
            scope.launch {
                val newRecipesFlow = repository.getRecipes().cachedIn(scope)
                dispatch(Msg.RecipesFlowUpdated(newRecipesFlow))
            }
        }
    }

    private object ReducerImpl : Reducer<State, Msg> {
        override fun State.reduce(message: Msg): State =
            when (message) {
                is Msg.SearchQueryChanged -> copy(searchQuery = message.query)
                is Msg.RecipesFlowUpdated -> copy(recipes = message.recipesFlow)
            }
    }
}
