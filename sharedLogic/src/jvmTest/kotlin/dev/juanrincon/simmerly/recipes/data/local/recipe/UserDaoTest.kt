package dev.juanrincon.simmerly.recipes.data.local.recipe

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import dev.juanrincon.simmerly.core.aUserEntity
import dev.juanrincon.simmerly.core.buildTestDatabase
import dev.juanrincon.simmerly.core.data.local.SimmerlyDatabase
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class UserDaoTest {

    private lateinit var db: SimmerlyDatabase
    private lateinit var dao: UserDao

    @BeforeTest
    fun setUp() {
        db = buildTestDatabase()
        dao = db.userDao()
    }

    @AfterTest
    fun tearDown() {
        db.close()
    }

    // region observeSelf

    @Test
    fun observeSelfEmitsNullWhenTableIsEmpty() = runTest {
        dao.observeSelf().test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun observeSelfEmitsUserAfterInsert() = runTest {
        dao.observeSelf().test {
            assertThat(awaitItem()).isNull()
            dao.upsert(aUserEntity(id = "user-1", fullName = "John Doe"))
            assertThat(awaitItem()?.fullName).isEqualTo("John Doe")
        }
    }

    @Test
    fun observeSelfEmitsUpdatedUserOnUpsert() = runTest {
        dao.observeSelf().test {
            assertThat(awaitItem()).isNull()
            dao.upsert(aUserEntity(id = "user-1", fullName = "John Doe"))
            assertThat(awaitItem()?.fullName).isEqualTo("John Doe")
            dao.upsert(aUserEntity(id = "user-1", fullName = "Jane Doe"))
            assertThat(awaitItem()?.fullName).isEqualTo("Jane Doe")
        }
    }

    // endregion

    // region upsert / upsertAll

    @Test
    fun upsertStoresUser() = runTest {
        dao.observeSelf().test {
            assertThat(awaitItem()).isNull()
            dao.upsert(aUserEntity(id = "user-1"))
            assertThat(awaitItem()?.id).isEqualTo("user-1")
        }
    }

    @Test
    fun upsertAllStoresUsers() = runTest {
        runBlocking {
            dao.upsertAll(listOf(aUserEntity(id = "user-1"), aUserEntity(id = "user-2")))
        }
        // observeSelf returns LIMIT 1; verify at least one user is stored
        dao.observeSelf().test {
            assertThat(awaitItem()).isNotNull()
        }
    }

    @Test
    fun upsertWithSameIdOverwritesUser() = runTest {
        dao.observeSelf().test {
            assertThat(awaitItem()).isNull()
            dao.upsert(aUserEntity(id = "user-1", fullName = "Old Name"))
            awaitItem() // first insert
            dao.upsert(aUserEntity(id = "user-1", fullName = "New Name"))
            assertThat(awaitItem()?.fullName).isEqualTo("New Name")
        }
    }

    // endregion
}
