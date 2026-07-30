package dev.juanrincon.simmerly.recipes.data.local.recent

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.ListRecipeWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface RecentlyViewedDao {
    @Upsert
    suspend fun upsert(entity: RecentlyViewedEntity)

    @Transaction
    @Query(
        """
        SELECT recipes.* FROM recipes
        INNER JOIN recently_viewed ON recipes.id = recently_viewed.recipeId
        ORDER BY recently_viewed.viewedAt DESC LIMIT 10
    """
    )
    fun observeWithRecipes(): Flow<List<ListRecipeWithTags>>
}
