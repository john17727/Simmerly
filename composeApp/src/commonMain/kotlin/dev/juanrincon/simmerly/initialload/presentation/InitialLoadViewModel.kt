package dev.juanrincon.simmerly.initialload.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import arrow.fx.coroutines.parZip
import dev.juanrincon.simmerly.initialload.domain.AuxiliaryRepository
import dev.juanrincon.simmerly.initialload.domain.UserRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class InitialLoadViewModel(
    private val userRepository: UserRepository,
    private val auxiliaryRepository: AuxiliaryRepository,
) : ViewModel() {

    private val _events = Channel<Unit>(Channel.BUFFERED)
    val events: Flow<Unit> = _events.receiveAsFlow()

    init {
        viewModelScope.launch {
            parZip(
                { userRepository.loadSelf() },
                { userRepository.loadSelfRatings() },
                { userRepository.loadSelfFavorites() },
                { auxiliaryRepository.loadTags() },
                { auxiliaryRepository.loadCategories() },
                { auxiliaryRepository.loadTools() },
                { auxiliaryRepository.loadFoods() },
                { auxiliaryRepository.loadUnits() },
            ) { user, ratings, favorites, tags, categories, tools, foods, units ->
                user.onLeft { println("InitialLoad: failed to load user — $it") }
                ratings.onLeft { println("InitialLoad: failed to load ratings — $it") }
                favorites.onLeft { println("InitialLoad: failed to load favorites — $it") }
                tags.onLeft { println("InitialLoad: failed to load tags — $it") }
                categories.onLeft { println("InitialLoad: failed to load categories — $it") }
                tools.onLeft { println("InitialLoad: failed to load tools — $it") }
                foods.onLeft { println("InitialLoad: failed to load foods — $it") }
                units.onLeft { println("InitialLoad: failed to load units — $it") }
            }
            _events.send(Unit)
        }
    }
}
