package dev.juanrincon.simmerly.shoppinglists.data.local

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.CacheKeys
import dev.juanrincon.simmerly.core.data.DefaultLocalDataCleaner
import dev.juanrincon.simmerly.core.data.Freshness
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import dev.juanrincon.simmerly.shoppinglists.aFood
import dev.juanrincon.simmerly.shoppinglists.aLabel
import dev.juanrincon.simmerly.shoppinglists.aRecipe
import dev.juanrincon.simmerly.shoppinglists.aRef
import dev.juanrincon.simmerly.shoppinglists.aShoppingList
import dev.juanrincon.simmerly.shoppinglists.aUnit
import dev.juanrincon.simmerly.shoppinglists.anItem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.hours

class ShoppingListStoreTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var freshness: Freshness
    private lateinit var store: ShoppingListStore

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        freshness = Freshness(db.cacheMetadataDao())
        store = ShoppingListStore(db, freshness)
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    private val fullList = aShoppingList(
        id = "list-1",
        name = "Sunday brunch",
        items = listOf(
            anItem(
                id = "item-1",
                quantity = 2.5,
                unit = aUnit(id = "cup"),
                food = aFood(id = "milk", pluralName = "milks", labelId = "dairy", labelName = "Dairy"),
                note = "oat",
                checked = true,
                position = 0,
                label = aLabel(id = "dairy", name = "Dairy"),
                recipeRefs = listOf(aRef("recipe-1", 2.0)),
            ),
            anItem(id = "item-2", note = "Coffee", position = 1),
        ),
        recipes = listOf(aRecipe("recipe-2", "Pancakes", 2.0), aRecipe("recipe-1", "Shakshuka")),
        labelOrder = listOf("dairy", "produce"),
    )

    @Test
    fun aListRoundTripsWithEveryField() = runTest {
        store.upsert(fullList)
        assertThat(store.get("list-1")).isEqualTo(fullList)
    }

    @Test
    fun emptyOptionalPartsRoundTrip() = runTest {
        val bare = aShoppingList(id = "bare", items = listOf(anItem(id = "i", listId = "bare")))
        store.upsert(bare)
        assertThat(store.get("bare")).isEqualTo(bare)
    }

    @Test
    fun upsertingAgainReplacesItemsAndRecipes() = runTest {
        store.upsert(fullList)
        store.upsert(fullList.copy(items = fullList.items.take(1), recipes = emptyList()))

        val stored = store.get("list-1")!!
        assertThat(stored.items.map { it.id }).containsExactly("item-1")
        assertThat(stored.recipes.isEmpty()).isTrue()
    }

    @Test
    fun listsAreOrderedByNameIgnoringCase() = runTest {
        store.upsert(aShoppingList(id = "b", name = "banana"))
        store.upsert(aShoppingList(id = "a", name = "Apple"))
        store.upsert(aShoppingList(id = "c", name = "cherry"))
        assertThat(store.observeAll().first().map { it.id }).containsExactly("a", "b", "c")
    }

    @Test
    fun itemsComeBackInPositionOrder() = runTest {
        store.upsert(
            aShoppingList(
                items = listOf(
                    anItem(id = "late", position = 5),
                    anItem(id = "early", position = 1),
                )
            )
        )
        assertThat(store.get("list-1")!!.items.map { it.id }).containsExactly("early", "late")
    }

    @Test
    fun deletingAListRemovesItsItemsToo() = runTest {
        store.upsert(fullList)
        store.upsert(aShoppingList(id = "other", items = listOf(anItem(id = "o1", listId = "other"))))

        store.delete("list-1")

        assertThat(store.get("list-1")).isNull()
        assertThat(db.shoppingListDao().get("other")!!.items.map { it.id }).containsExactly("o1")
        // Nothing left behind for the deleted list.
        db.shoppingListDao().deleteItems("other")
        assertThat(db.shoppingListDao().observeAll().first().flatMap { it.items }.isEmpty()).isTrue()
    }

    // region update

    @Test
    fun updateAppliesTheTransformAndReturnsThePreviousList() = runTest {
        store.upsert(fullList)

        val previous = store.update("list-1") { it.copy(name = "Renamed") }

        assertThat(previous).isEqualTo(fullList)
        assertThat(store.get("list-1")!!.name).isEqualTo("Renamed")
    }

    @Test
    fun updateOfAnUnknownListWritesNothing() = runTest {
        val previous = store.update("missing") { it.copy(name = "x") }
        assertThat(previous).isNull()
        assertThat(store.observeAll().first().isEmpty()).isTrue()
    }

    // endregion

    // region loaded vs. empty

    @Test
    fun nothingLoadedYetIsNullNotEmpty() = runTest {
        assertThat(store.observeAllOrNull().first()).isNull()
    }

    @Test
    fun aSingleStoredListCountsAsLoaded() = runTest {
        store.upsert(aShoppingList())
        assertThat(store.observeAllOrNull().first()).isNotNull()
    }

    @Test
    fun aFullRefreshWithNoListsIsEmptyNotNull() = runTest {
        store.replaceAll(emptyList())
        assertThat(store.observeAllOrNull().first()).isEqualTo(emptyList())
    }

    @Test
    fun observersSeeTheTransitionFromLoadingToLoaded() = runTest {
        store.observeAllOrNull().test {
            assertThat(awaitItem()).isNull()
            store.replaceAll(listOf(aShoppingList()))
            assertThat(awaitItem()!!.map { it.id }).containsExactly("list-1")
        }
    }

    // endregion

    // region replaceAll

    @Test
    fun replaceAllRemovesListsThatAreNoLongerOnTheServer() = runTest {
        store.upsert(aShoppingList(id = "gone"))
        store.upsert(aShoppingList(id = "kept", name = "old"))

        store.replaceAll(listOf(aShoppingList(id = "kept", name = "new"), aShoppingList(id = "added", name = "added")))

        val lists = store.observeAll().first()
        assertThat(lists.map { it.id }).containsExactly("added", "kept")
        assertThat(lists.first { it.id == "kept" }.name).isEqualTo("new")
    }

    @Test
    fun replaceAllMarksTheListsSynced() = runTest {
        assertThat(freshness.isStale(CacheKeys.SHOPPING_LISTS, 1.hours)).isTrue()
        store.replaceAll(emptyList())
        assertThat(freshness.isStale(CacheKeys.SHOPPING_LISTS, 1.hours)).isFalse()
    }

    @Test
    fun upsertingASingleListDoesNotMarkTheCollectionSynced() = runTest {
        store.upsert(aShoppingList())
        assertThat(freshness.isStale(CacheKeys.SHOPPING_LISTS, 1.hours)).isTrue()
    }

    // endregion

    @Test
    fun clearingLocalDataEmptiesShoppingListsAndTheirLoadedState() = runTest {
        store.replaceAll(listOf(fullList))

        DefaultLocalDataCleaner(db, emptyList()).clearAll()

        assertThat(store.get("list-1")).isNull()
        assertThat(store.observeAllOrNull().first()).isNull()
    }
}
