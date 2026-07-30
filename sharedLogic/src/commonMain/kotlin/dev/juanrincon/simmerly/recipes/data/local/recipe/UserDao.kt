package dev.juanrincon.simmerly.recipes.data.local.recipe

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Upsert
    suspend fun upsertAll(users: List<UserEntity>)

    @Upsert
    suspend fun upsert(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    fun observeSelf(): Flow<UserEntity?>
}