package dev.juanrincon.simmerly.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import dev.juanrincon.simmerly.navigation.auth.AuthNavigationViewModel
import dev.juanrincon.simmerly.profile.presentation.ProfileViewModel
import dev.juanrincon.simmerly.initialload.presentation.InitialLoadViewModel
import dev.juanrincon.simmerly.recipes.presentation.comments.RecipeCommentsViewModel
import dev.juanrincon.simmerly.recipes.presentation.cookmode.CookModeViewModel
import dev.juanrincon.simmerly.recipes.presentation.details.RecipeDetailsViewModel
import dev.juanrincon.simmerly.recipes.presentation.list.RecipeListViewModel
import dev.juanrincon.simmerly.recipes.presentation.search.RecipeSearchViewModel
import dev.juanrincon.simmerly.welcome.presentation.WelcomeViewModel
import org.koin.core.parameter.parametersOf

/**
 * Resolves ViewModels from the shared Koin graph for Swift. Each instance is tracked in its own
 * [ViewModelStore] so [dispose] can invoke `onCleared()` and cancel `viewModelScope`, mirroring
 * what the Android/Compose ViewModelStoreOwner does automatically.
 */
object SimmerlyViewModels {

    private val stores = mutableMapOf<ViewModel, ViewModelStore>()

    private fun <T : ViewModel> track(viewModel: T): T {
        stores[viewModel] = ViewModelStore().apply { put("vm", viewModel) }
        return viewModel
    }

    fun dispose(viewModel: ViewModel) {
        stores.remove(viewModel)?.clear()
    }

    fun welcome(): WelcomeViewModel = track(simmerlyKoin.koin.get())

    fun recipeList(): RecipeListViewModel = track(simmerlyKoin.koin.get())

    fun recipeDetails(recipeId: String): RecipeDetailsViewModel =
        track(simmerlyKoin.koin.get { parametersOf(recipeId) })

    fun recipeComments(recipeId: String): RecipeCommentsViewModel =
        track(simmerlyKoin.koin.get { parametersOf(recipeId) })

    fun cookMode(recipeId: String): CookModeViewModel =
        track(simmerlyKoin.koin.get { parametersOf(recipeId) })

    fun recipeSearch(): RecipeSearchViewModel = track(simmerlyKoin.koin.get())

    fun authNavigation(): AuthNavigationViewModel = track(simmerlyKoin.koin.get())

    fun profile(): ProfileViewModel = track(simmerlyKoin.koin.get())

    fun initialLoad(): InitialLoadViewModel = track(simmerlyKoin.koin.get())
}
