package dev.juanrincon.simmerly.profile

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.profile.presentation.ProfileViewModel
import dev.juanrincon.simmerly.profile.presentation.model.UiUser
import dev.juanrincon.simmerly.recipes.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeProfileRepository
    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeProfileRepository()
        viewModel = ProfileViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial state

    @Test
    fun initialUserIsNull() = runTest {
        viewModel.user.test {
            assertThat(awaitItem()).isNull()
        }
    }

    // endregion

    // region Name mapping

    @Test
    fun userWithFullNameMapsToFirstNameOnly() = runTest {
        repo.emitUser(aUser(fullName = "John Doe"))
        viewModel.user.test {
            assertThat(awaitItem()?.name).isEqualTo("John")
        }
    }

    @Test
    fun userWithThreePartNameMapsToFirstNameOnly() = runTest {
        repo.emitUser(aUser(fullName = "Mary Jane Watson"))
        viewModel.user.test {
            assertThat(awaitItem()?.name).isEqualTo("Mary")
        }
    }

    @Test
    fun userWithSingleWordNameKeepsThatName() = runTest {
        repo.emitUser(aUser(fullName = "Alice"))
        viewModel.user.test {
            assertThat(awaitItem()?.name).isEqualTo("Alice")
        }
    }

    // endregion

    // region Image mapping

    @Test
    fun userImageIsPassedThrough() = runTest {
        repo.emitUser(aUser(image = "https://example.com/pic.jpg"))
        viewModel.user.test {
            assertThat(awaitItem()?.image).isEqualTo("https://example.com/pic.jpg")
        }
    }

    // endregion

    // region Null handling

    @Test
    fun nullUserEmitsNullUiUser() = runTest {
        repo.emitUser(null)
        viewModel.user.test {
            assertThat(awaitItem()).isNull()
        }
    }

    @Test
    fun transitionsFromNullToUser() = runTest {
        viewModel.user.test {
            assertThat(awaitItem()).isNull()
            repo.emitUser(aUser(fullName = "Jane Doe", image = "https://example.com/jane.jpg"))
            assertThat(awaitItem()).isEqualTo(UiUser(name = "Jane", image = "https://example.com/jane.jpg"))
        }
    }

    @Test
    fun transitionsFromUserToNull() = runTest {
        repo.emitUser(aUser(fullName = "John Doe"))
        viewModel.user.test {
            assertThat(awaitItem()?.name).isEqualTo("John")
            repo.emitUser(null)
            assertThat(awaitItem()).isNull()
        }
    }

    // endregion

    private fun aUser(
        id: String = "user-1",
        fullName: String = "Test User",
        image: String = ""
    ) = User(id = id, username = "test", admin = false, fullName = fullName, image = image)
}
