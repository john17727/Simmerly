package dev.juanrincon.simmerly.recipes.presentation.list.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class DefaultRecipeListComponent(
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val repository: RecipeRepository,
    val onRecipeSelected: (String) -> Unit
) : RecipeListComponent,
    ComponentContext by componentContext {

    private val store =
        instanceKeeper.getStore { RecipeListStoreFactory(storeFactory, repository).create() }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<RecipeListStore.State> = store.stateFlow
    override val labels: Flow<RecipeListStore.Label> = store.labels

    override fun onEvent(event: RecipeListStore.Intent) = store.accept(event)

    override fun onOutput(output: RecipeListStore.Output) = when (output) {
        is RecipeListStore.Output.SelectedRecipe -> onRecipeSelected(output.recipeId)
    }
}