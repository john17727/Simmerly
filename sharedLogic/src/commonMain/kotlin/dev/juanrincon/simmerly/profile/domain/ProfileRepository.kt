package dev.juanrincon.simmerly.profile.domain

import dev.juanrincon.simmerly.recipes.domain.model.User
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    fun observeUser(): Flow<User?>
}
