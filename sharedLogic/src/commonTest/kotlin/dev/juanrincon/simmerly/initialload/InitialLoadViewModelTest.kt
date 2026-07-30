package dev.juanrincon.simmerly.initialload

import app.cash.turbine.test
import app.tracktion.core.domain.util.DataError
import arrow.core.left
import assertk.assertThat
import assertk.assertions.isEqualTo
import dev.juanrincon.simmerly.initialload.presentation.InitialLoadViewModel
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
class InitialLoadViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var userRepository: FakeUserRepository
    private lateinit var auxiliaryRepository: FakeAuxiliaryRepository

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        userRepository = FakeUserRepository()
        auxiliaryRepository = FakeAuxiliaryRepository()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Event emission

    @Test
    fun emitsUnitEventWhenAllLoadsSucceed() = runTest {
        val viewModel = InitialLoadViewModel(userRepository, auxiliaryRepository)
        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun emitsUnitEventEvenWhenAllLoadsFail() = runTest {
        userRepository.loadSelfResult = DataError.NetworkError.Unknown.left()
        userRepository.loadSelfRatingsResult = DataError.NetworkError.Unknown.left()
        userRepository.loadSelfFavoritesResult = DataError.NetworkError.Unknown.left()
        auxiliaryRepository.loadTagsResult = DataError.NetworkError.Unknown.left()
        auxiliaryRepository.loadCategoriesResult = DataError.NetworkError.Unknown.left()
        auxiliaryRepository.loadToolsResult = DataError.NetworkError.Unknown.left()
        auxiliaryRepository.loadUnitsResult = DataError.NetworkError.Unknown.left()

        val viewModel = InitialLoadViewModel(userRepository, auxiliaryRepository)
        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    @Test
    fun emitsUnitEventWhenSomeLoadsFail() = runTest {
        userRepository.loadSelfResult = DataError.NetworkError.Unknown.left()
        auxiliaryRepository.loadTagsResult = DataError.NetworkError.ServerError.left()

        val viewModel = InitialLoadViewModel(userRepository, auxiliaryRepository)
        viewModel.events.test {
            assertThat(awaitItem()).isEqualTo(Unit)
        }
    }

    // endregion

    // region Repository call verification

    @Test
    fun callsAllSevenRepositoryMethodsOnCreation() = runTest {
        InitialLoadViewModel(userRepository, auxiliaryRepository)

        assertThat(userRepository.loadSelfCallCount).isEqualTo(1)
        assertThat(userRepository.loadSelfRatingsCallCount).isEqualTo(1)
        assertThat(userRepository.loadSelfFavoritesCallCount).isEqualTo(1)
        assertThat(auxiliaryRepository.loadTagsCallCount).isEqualTo(1)
        assertThat(auxiliaryRepository.loadCategoriesCallCount).isEqualTo(1)
        assertThat(auxiliaryRepository.loadToolsCallCount).isEqualTo(1)
        assertThat(auxiliaryRepository.loadUnitsCallCount).isEqualTo(1)
    }

    // endregion
}
