package dev.juanrincon.simmerly.shoppinglists.data.local

import androidx.room3.Dao
import androidx.room3.Insert
import androidx.room3.OnConflictStrategy
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ShoppingListDao {
    @Transaction
    @Query("SELECT * FROM shopping_lists")
    fun observeAll(): Flow<List<ShoppingListWithRelations>>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    fun observe(id: String): Flow<ShoppingListWithRelations?>

    @Transaction
    @Query("SELECT * FROM shopping_lists WHERE id = :id")
    suspend fun get(id: String): ShoppingListWithRelations?

    /** An upsert, not a replace: replacing the row would cascade away its items and recipes. */
    @Upsert
    suspend fun upsertList(list: ShoppingListEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<ShoppingListItemEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecipes(recipes: List<ShoppingListRecipeEntity>)

    @Query("DELETE FROM shopping_list_items WHERE list_id = :listId")
    suspend fun deleteItems(listId: String)

    @Query("DELETE FROM shopping_list_recipes WHERE list_id = :listId")
    suspend fun deleteRecipes(listId: String)

    @Query("DELETE FROM shopping_lists WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM shopping_lists WHERE id NOT IN (:keepIds)")
    suspend fun deleteAllExcept(keepIds: List<String>)
}
