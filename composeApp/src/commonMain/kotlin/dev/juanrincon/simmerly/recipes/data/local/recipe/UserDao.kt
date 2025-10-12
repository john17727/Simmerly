package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room.Dao
import androidx.room.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    @Upsert
    suspend fun upsert(user: UserEntity)
}