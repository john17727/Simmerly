package dev.juanrincon.simmerly.recipes.presentation.details.decompose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.mvikotlin.core.instancekeeper.getStore
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
import dev.juanrincon.simmerly.core.presentation.AppBarAction
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Intent
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.Label
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStore.State
import dev.juanrincon.simmerly.recipes.presentation.details.mvikotlin.RecipeDetailsStoreFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

class DefaultRecipeDetailsComponent(
    private val recipeId: String,
    componentContext: ComponentContext,
    storeFactory: StoreFactory,
    private val repository: RecipeRepository
) : RecipeDetailsComponent,
    ComponentContext by componentContext {
    private val store = instanceKeeper.getStore {
        RecipeDetailsStoreFactory(
            recipeId,
            storeFactory,
            repository
        ).create()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override val state: StateFlow<State> = store.stateFlow

    override val labels: Flow<Label> = store.labels

    override fun onEvent(event: Intent) = store.accept(event)
    override val actions: List<AppBarAction> = listOf(
        AppBarAction(
            icon = {
                Icon(Icons.Default.Settings, contentDescription = "")
            },
            onClick = {
               onEvent(Intent.ShowSettings)
            }
        )
    )
}