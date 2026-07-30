package dev.juanrincon.simmerly.profile.data

import dev.juanrincon.simmerly.auth.domain.SessionDataStore
import dev.juanrincon.simmerly.profile.domain.ProfileRepository
import dev.juanrincon.simmerly.recipes.data.local.recipe.UserDao
import dev.juanrincon.simmerly.recipes.data.mappers.toDomain
import dev.juanrincon.simmerly.recipes.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class DefaultProfileRepository(
    private val userDao: UserDao,
    private val sessionDataStore: SessionDataStore,
) : ProfileRepository {

    override fun observeUser(): Flow<User?> =
        sessionDataStore.observeServerAddress()
            .combine(userDao.observeSelf()) { host, entity ->
                entity?.toDomain(host)
            }
}
