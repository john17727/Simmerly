package dev.juanrincon.simmerly.welcome

import app.cash.turbine.test
import arrow.core.left
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import androidx.compose.foundation.text.input.setTextAndPlaceCursorAtEnd
import dev.juanrincon.simmerly.auth.FakeAuthRepository
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.welcome.presentation.WelcomeViewModel
import dev.juanrincon.simmerly.welcome.presentation.model.CredentialType
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeIntent
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeSideEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import simmerly.shared.generated.resources.Res
import simmerly.shared.generated.resources.login_failed
import simmerly.shared.generated.resources.something_went_wrong
import simmerly.shared.generated.resources.unreachable_server_address
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WelcomeViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: WelcomeViewModel

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAuthRepository()
        viewModel = WelcomeViewModel(repo)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial state

    @Test
    fun initialStateHasEmptyFieldsCredentialsTypeAndLoginButtonDisabled() = runTest {
        viewModel.container.stateFlow.test {
            val state = awaitItem()
            assertThat(state.serverAddress.text.toString()).isEqualTo("")
            assertThat(state.username.text.toString()).isEqualTo("")
            assertThat(state.password.text.toString()).isEqualTo("")
            assertThat(state.credentialType).isEqualTo(CredentialType.CREDENTIALS)
            assertThat(state.isLoading).isFalse()
            assertThat(state.isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Credential type

    @Test
    fun onCredentialTypeChangedCredentialsToApiTokenUpdatesCredentialType() = runTest {
        viewModel.container.stateFlow.test {
            skipItems(1)
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
            assertThat(awaitItem().credentialType).isEqualTo(CredentialType.API_TOKEN)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onCredentialTypeChangedApiTokenBackToCredentialsUpdatesCredentialType() = runTest {
        viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
        viewModel.container.stateFlow.test {
            skipItems(1)
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.CREDENTIALS))
            assertThat(awaitItem().credentialType).isEqualTo(CredentialType.CREDENTIALS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region isLoginButtonEnabled — CREDENTIALS mode

    @Test
    fun loginButtonDisabledWhenOnlyServerAddressFilledInCredentialsMode() {
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonDisabledWhenServerAddressAndUsernameFilledButPasswordEmpty() {
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.username.setTextAndPlaceCursorAtEnd("user")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonDisabledWhenServerAddressAndPasswordFilledButUsernameEmpty() {
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.password.setTextAndPlaceCursorAtEnd("pass")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonEnabledWhenAllCredentialsFieldsNonBlank() {
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.username.setTextAndPlaceCursorAtEnd("user")
        state.password.setTextAndPlaceCursorAtEnd("pass")
        assertThat(state.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun loginButtonDisabledAgainAfterClearingUsername() {
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.username.setTextAndPlaceCursorAtEnd("user")
        state.password.setTextAndPlaceCursorAtEnd("pass")
        assertThat(state.isLoginButtonEnabled).isTrue()
        state.username.setTextAndPlaceCursorAtEnd("")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    // endregion

    // region isLoginButtonEnabled — API_TOKEN mode

    @Test
    fun loginButtonDisabledWhenOnlyServerAddressFilledInApiTokenMode() {
        viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonEnabledWhenServerAddressAndTokenNonBlankInApiTokenMode() {
        viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.password.setTextAndPlaceCursorAtEnd("my-token")
        assertThat(state.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun switchingToApiTokenEnablesButtonWhenServerAddressAndPasswordFilledWithoutUsername() {
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.password.setTextAndPlaceCursorAtEnd("my-token")
        viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
        assertThat(viewModel.container.stateFlow.value.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun switchingBackToCredentialsDisablesButtonWhenUsernameBlank() {
        val state = viewModel.container.stateFlow.value
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.password.setTextAndPlaceCursorAtEnd("my-token")
        viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
        viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.CREDENTIALS))
        assertThat(viewModel.container.stateFlow.value.isLoginButtonEnabled).isFalse()
    }

    // endregion

    // region https:// prepending

    @Test
    fun loginWithPlainDomainPrependsHttpsPrefix() = runTest {
        fillCredentials(serverAddress = "myserver.com")
        viewModel.onEvent(WelcomeIntent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithExistingHttpsPrefixDoesNotDoublePrepend() = runTest {
        fillCredentials(serverAddress = "https://myserver.com")
        viewModel.onEvent(WelcomeIntent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithHttpPrefixPassesThroughUnchanged() = runTest {
        fillCredentials(serverAddress = "http://myserver.com")
        viewModel.onEvent(WelcomeIntent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("http://myserver.com")
    }

    // endregion

    // region Loading state lifecycle

    @Test
    fun isLoadingTrueDuringLoginAndFalseAfterSuccess() = runTest {
        repo.shouldDelayLogin = true
        fillCredentials()

        viewModel.container.stateFlow.test {
            skipItems(1)
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            assertThat(awaitItem().isLoading).isTrue()
            repo.releaseLogin()
            assertThat(awaitItem().isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun isLoadingTrueDuringLoginAndFalseAfterFailure() = runTest {
        repo.shouldDelayLogin = true
        repo.loginResult = LoginError.InvalidCredentials.left()
        fillCredentials()

        viewModel.container.stateFlow.test {
            skipItems(1)
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            assertThat(awaitItem().isLoading).isTrue()
            repo.releaseLogin()
            assertThat(awaitItem().isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Login success path

    @Test
    fun successfulLoginDoesNotEmitLoginFailedSideEffect() = runTest {
        fillCredentials()
        viewModel.container.sideEffectFlow.test {
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            expectNoEvents()
        }
    }

    @Test
    fun credentialsLoginPassesServerAddressUsernameAndPasswordToRepository() = runTest {
        fillCredentials(serverAddress = "server.com", username = "admin", password = "secret")
        viewModel.onEvent(WelcomeIntent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastCredentialsLoginCall?.second).isEqualTo("admin")
        assertThat(repo.lastCredentialsLoginCall?.third).isEqualTo("secret")
    }

    @Test
    fun apiTokenLoginPassesServerAddressAndTokenToRepository() = runTest {
        val s = viewModel.container.stateFlow.value
        s.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        s.password.setTextAndPlaceCursorAtEnd("my-api-token")
        viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
        viewModel.onEvent(WelcomeIntent.OnLoginClicked)
        assertThat(repo.lastApiTokenLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastApiTokenLoginCall?.second).isEqualTo("my-api-token")
    }

    // endregion

    // region Error side effects

    @Test
    fun invalidCredentialsErrorEmitsLoginFailedWithLoginFailedResource() = runTest {
        repo.loginResult = LoginError.InvalidCredentials.left()
        fillCredentials()
        viewModel.container.sideEffectFlow.test {
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            val sideEffect = awaitItem()
            assertThat(sideEffect).isInstanceOf(WelcomeSideEffect.LoginFailed::class)
            assertThat((sideEffect as WelcomeSideEffect.LoginFailed).message)
                .isEqualTo(Res.string.login_failed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun networkErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.NetworkError.left()
        fillCredentials()
        viewModel.container.sideEffectFlow.test {
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            val sideEffect = awaitItem() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(Res.string.unreachable_server_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unresolvedAddressErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.UnresolvedAddress.left()
        fillCredentials()
        viewModel.container.sideEffectFlow.test {
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            val sideEffect = awaitItem() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(Res.string.unreachable_server_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unknownErrorEmitsLoginFailedWithSomethingWentWrongResource() = runTest {
        repo.loginResult = LoginError.UnknownError.left()
        fillCredentials()
        viewModel.container.sideEffectFlow.test {
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            val sideEffect = awaitItem() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(Res.string.something_went_wrong)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Double-submit guard

    @Test
    fun secondLoginClickWhileInFlightDoesNotTriggerSecondRepositoryCall() = runTest {
        repo.shouldDelayLogin = true
        fillCredentials()
        viewModel.onEvent(WelcomeIntent.OnLoginClicked)
        viewModel.onEvent(WelcomeIntent.OnLoginClicked)
        repo.releaseLogin()
        assertThat(repo.loginCallCount).isEqualTo(1)
    }

    // endregion

    // region Helpers

    private fun fillCredentials(
        serverAddress: String = "server.com",
        username: String = "user",
        password: String = "pass"
    ) {
        val s = viewModel.container.stateFlow.value
        s.serverAddress.setTextAndPlaceCursorAtEnd(serverAddress)
        s.username.setTextAndPlaceCursorAtEnd(username)
        s.password.setTextAndPlaceCursorAtEnd(password)
    }

    // endregion
}
