package dev.juanrincon.simmerly.profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.juanrincon.simmerly.profile.domain.ProfileRepository
import dev.juanrincon.simmerly.profile.presentation.model.UiUser
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class ProfileViewModel(repository: ProfileRepository) : ViewModel() {

    val user: StateFlow<UiUser?> = repository.observeUser()
        .map { user ->
            user?.let {
                UiUser(
                    name = it.fullName.substringBefore(" "),
                    image = it.image,
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)
}
