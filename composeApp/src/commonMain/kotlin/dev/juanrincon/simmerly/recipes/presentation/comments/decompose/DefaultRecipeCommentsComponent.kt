package dev.juanrincon.simmerly.recipes.presentation.comments.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStore
import dev.juanrincon.simmerly.recipes.presentation.comments.mvikotlin.RecipeCommentsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.StateFlow

class DefaultRecipeCommentsComponent(
    private val recipeId: String,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val repository: RecipeRepository
) : RecipeCommentsComponent, ComponentContext by componentContext {
    private val store = instanceKeeper.getStore {
        RecipeCommentsStoreFactory(
            recipeId,
            storeFactory,
            repository
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RecipeCommentsStore.State> = store.stateFlow

    override fun onEvent(event: RecipeCommentsStore.Intent) = store.accept(event)
}