package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class RecipeDetailsViewModel(
    recipeId: String,
    storeFactory: StoreFactory,
    repository: RecipeRepository,
) : ViewModel() {

    private val store = RecipeDetailsStoreFactory(recipeId, storeFactory, repository).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<State> = store.stateFlow

    val labels: Flow<Label> = store.labels

    fun onEvent(event: Intent) = store.accept(event)

    override fun onCleared() {
        store.dispose()
    }
}
