package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStore
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipesStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class DefaultRecipeListComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val repository: RecipeRepository
) : RecipeListComponent,
    ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore { RecipesStoreFactory(storeFactory, repository).create() }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RecipesStore.State> = store.stateFlow
    override val labels: Flow<RecipesStore.Label> = store.labels

    override fun onEvent(event: RecipesStore.Intent) = store.accept(event)
}