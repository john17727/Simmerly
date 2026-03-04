package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

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

    val commentsEnabled: StateFlow<Boolean> = state
        .map { !it.recipe.settings.disableComments }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val appBarActions: List<AppBarAction> = listOf(
        AppBarAction(
            icon = { Icon(Icons.Default.Settings, contentDescription = "") },
            onClick = { onEvent(Intent.ShowSettings) }
        )
    )

    override fun onCleared() {
        store.dispose()
    }
}
