package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    @Upsert
    suspend fun upsert(user: UserEntity)
}