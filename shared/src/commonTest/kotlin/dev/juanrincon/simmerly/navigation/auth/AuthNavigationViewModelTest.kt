package dev.juanrincon.simmerly.navigation.auth

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.auth.FakeAuthRepository
import dev.juanrincon.simmerly.auth.domain.AuthState
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
class AuthNavigationViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: AuthNavigationViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAuthRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region State mapping

    @Test
    fun unauthenticatedStateMapsToLoginDestination() = runTest {
        repo.emitAuthState(AuthState.Unauthenticated)
        viewModel = AuthNavigationViewModel(repo)
        viewModel.isAuthenticated.test {
            assertThat(awaitItem()).isEqualTo(AuthDestinations.Login)
        }
    }

    @Test
    fun authenticatedStateMapsToInitialLoadDestination() = runTest {
        repo.emitAuthState(AuthState.Authenticated)
        viewModel = AuthNavigationViewModel(repo)
        viewModel.isAuthenticated.test {
            assertThat(awaitItem()).isEqualTo(AuthDestinations.InitialLoad)
        }
    }

    @Test
    fun loadingStateMapsToSplashDestination() = runTest {
        repo.emitAuthState(AuthState.Loading)
        viewModel = AuthNavigationViewModel(repo)
        viewModel.isAuthenticated.test {
            assertThat(awaitItem()).isEqualTo(AuthDestinations.Splash)
        }
    }

    // endregion

    // region State transitions

    @Test
    fun transitionsFromLoginToInitialLoadWhenAuthenticated() = runTest {
        repo.emitAuthState(AuthState.Unauthenticated)
        viewModel = AuthNavigationViewModel(repo)
        viewModel.isAuthenticated.test {
            assertThat(awaitItem()).isEqualTo(AuthDestinations.Login)
            repo.emitAuthState(AuthState.Authenticated)
            assertThat(awaitItem()).isEqualTo(AuthDestinations.InitialLoad)
        }
    }

    @Test
    fun transitionsFromSplashToLoginWhenUnauthenticated() = runTest {
        repo.emitAuthState(AuthState.Loading)
        viewModel = AuthNavigationViewModel(repo)
        viewModel.isAuthenticated.test {
            assertThat(awaitItem()).isEqualTo(AuthDestinations.Splash)
            repo.emitAuthState(AuthState.Unauthenticated)
            assertThat(awaitItem()).isEqualTo(AuthDestinations.Login)
        }
    }

    @Test
    fun transitionsFromInitialLoadToLoginOnLogout() = runTest {
        repo.emitAuthState(AuthState.Authenticated)
        viewModel = AuthNavigationViewModel(repo)
        viewModel.isAuthenticated.test {
            assertThat(awaitItem()).isEqualTo(AuthDestinations.InitialLoad)
            repo.emitAuthState(AuthState.Unauthenticated)
            assertThat(awaitItem()).isEqualTo(AuthDestinations.Login)
        }
    }

    // endregion
}
