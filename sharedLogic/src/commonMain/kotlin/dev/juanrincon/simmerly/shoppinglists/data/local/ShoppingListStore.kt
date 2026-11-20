package dev.juanrincon.simmerly.shoppinglists.data.local

import androidx.room3.immediateTransaction
import androidx.room3.useWriterConnection
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.CacheTtl
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.shoppinglists.data.mappers.itemEntities
import dev.juanrincon.simmerly.shoppinglists.data.mappers.recipeEntities
import dev.juanrincon.simmerly.shoppinglists.data.mappers.toDomain
import dev.juanrincon.simmerly.shoppinglists.data.mappers.toEntity
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

/**
 * Shopping lists as domain objects, persisted in Room. Whole lists are written at a time (a list
 * is a few dozen rows at most), each in one transaction, so observers never see a half-written
 * list.
 */
class ShoppingListStore(
    private val database: SimmerlyDatabase,
    private val freshness: Freshness,
) {
    private val dao = database.shoppingListDao()

    /** Every stored list, ordered by name. */
    fun observeAll(): Flow<List<ShoppingList>> = dao.observeAll().map { all ->
        all.map { it.toDomain() }.sortedByName()
    }

    /**
     * Like [observeAll], but `null` while nothing has been loaded yet — no list is stored and no
     * full refresh has completed — so a screen can tell "still loading" from "no lists".
     */
    fun observeAllOrNull(): Flow<List<ShoppingList>?> = combine(
        observeAll(),
        database.cacheMetadataDao().observe(CacheKeys.SHOPPING_LISTS),
    ) { lists, synced -> if (lists.isEmpty() && synced == null) null else lists }

    /** Whether the full set of lists was never synced or its freshness window has passed. */
    suspend fun isStale(): Boolean = freshness.isStale(CacheKeys.SHOPPING_LISTS, CacheTtl.SHOPPING_LISTS)

    fun observe(id: String): Flow<ShoppingList?> = dao.observe(id).map { it?.toDomain() }

    suspend fun get(id: String): ShoppingList? = dao.get(id)?.toDomain()

    suspend fun upsert(list: ShoppingList) {
        write { writeList(list) }
    }

    /**
     * Makes [lists] the full set of stored lists: they are written and every other list is
     * removed, in one transaction. This is what a full refresh means, and it is also what marks
     * the lists as synced.
     */
    suspend fun replaceAll(lists: List<ShoppingList>) {
        write {
            dao.deleteAllExcept(lists.map { it.id })
            lists.forEach { writeList(it) }
        }
        freshness.markSynced(CacheKeys.SHOPPING_LISTS)
    }

    suspend fun delete(id: String) {
        write { dao.delete(id) }
    }

    /**
     * Reads list [id], applies [transform] and writes the result back as one atomic step.
     * Returns the list as it was before, or `null` if there is no such list (nothing is written).
     */
    suspend fun update(id: String, transform: (ShoppingList) -> ShoppingList): ShoppingList? {
        var previous: ShoppingList? = null
        write {
            previous = dao.get(id)?.toDomain()?.also { writeList(transform(it)) }
        }
        return previous
    }

    private suspend fun write(block: suspend () -> Unit) {
        database.useWriterConnection { transactor ->
            transactor.immediateTransaction { block() }
        }
    }

    private suspend fun writeList(list: ShoppingList) {
        dao.upsertList(list.toEntity())
        dao.deleteItems(list.id)
        dao.deleteRecipes(list.id)
        dao.insertItems(list.itemEntities())
        dao.insertRecipes(list.recipeEntities())
    }
}

internal fun List<ShoppingList>.sortedByName() = sortedBy { it.name.lowercase() }
