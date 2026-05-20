package dev.juanrincon.simmerly.recipes.presentation.comments

import androidx.lifecycle.ViewModel
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class RecipeCommentsViewModel(
    recipeId: String,
    storeFactory: StoreFactory,
    repository: RecipeRepository,
) : ViewModel() {

    private val store = RecipeCommentsStoreFactory(recipeId, storeFactory, repository).create()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<RecipeCommentsStore.State> = store.stateFlow

    fun onEvent(event: RecipeCommentsStore.Intent) = store.accept(event)

    override fun onCleared() {
        store.dispose()
    }
}
