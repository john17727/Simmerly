package dev.juanrincon.simmerly.recipes.presentation.list

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RecipeListViewModel(
    recipeListStoreFactory: RecipeListStoreFactory,
) : ViewModel() {

    private val store = recipeListStoreFactory.create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RecipeListStore.State> = store.stateFlow

    val labels: Flow<RecipeListStore.Label> = store.labels

    fun onEvent(event: RecipeListStore.Intent) = store.accept(event)

    override fun onCleared() {
        store.dispose()
    }
}
