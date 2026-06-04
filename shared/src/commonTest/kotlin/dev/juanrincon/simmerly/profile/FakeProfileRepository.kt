package dev.juanrincon.simmerly.profile

import dev.juanrincon.simmerly.profile.domain.ProfileRepository
import dev.juanrincon.simmerly.recipes.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeProfileRepository : ProfileRepository {
    private val userFlow = MutableStateFlow<User?>(null)

    override fun observeUser(): Flow<User?> = userFlow

    fun emitUser(user: User?) {
        userFlow.value = user
    }
}
