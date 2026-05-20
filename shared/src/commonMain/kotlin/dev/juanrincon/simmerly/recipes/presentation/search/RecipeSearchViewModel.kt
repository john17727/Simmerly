package dev.juanrincon.simmerly.recipes.presentation.search

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStore
import dev.juanrincon.simmerly.recipes.presentation.search.mvikotlin.RecipeSearchStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RecipeSearchViewModel(
    recipeSearchStoreFactory: RecipeSearchStoreFactory
) : ViewModel() {

    private val store = recipeSearchStoreFactory.create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RecipeSearchStore.State> = store.stateFlow

    val labels: Flow<RecipeSearchStore.Label> = store.labels

    fun onEvent(intent: RecipeSearchStore.Intent) = store.accept(intent)

    override fun onCleared() {
        store.dispose()
    }
}
