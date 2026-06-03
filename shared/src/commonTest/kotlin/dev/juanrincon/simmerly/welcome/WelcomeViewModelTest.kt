package dev.juanrincon.simmerly.welcome

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
import dev.juanrincon.simmerly.welcome.presentation.orbit.WelcomeState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.orbitmvi.orbit.test.test
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
    fun initialStateHasEmptyFieldsCredentialsTypeAndLoginButtonDisabled() {
        val state = viewModel.container.stateFlow.value
        assertThat(state.serverAddress.text.toString()).isEqualTo("")
        assertThat(state.username.text.toString()).isEqualTo("")
        assertThat(state.password.text.toString()).isEqualTo("")
        assertThat(state.credentialType).isEqualTo(CredentialType.CREDENTIALS)
        assertThat(state.isLoading).isFalse()
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    // endregion

    // region Credential type

    @Test
    fun onCredentialTypeChangedCredentialsToApiTokenUpdatesCredentialType() = runTest {
        viewModel.test(this) {
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
            assertThat(awaitState().credentialType).isEqualTo(CredentialType.API_TOKEN)
        }
    }

    @Test
    fun onCredentialTypeChangedApiTokenBackToCredentialsUpdatesCredentialType() = runTest {
        viewModel.test(this) {
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
            awaitState() // consume API_TOKEN state
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.CREDENTIALS))
            assertThat(awaitState().credentialType).isEqualTo(CredentialType.CREDENTIALS)
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
    // These test WelcomeState's computed property directly, bypassing the ViewModel,
    // since TextFieldState mutations never flow through the Orbit reducer.

    @Test
    fun loginButtonDisabledWhenOnlyServerAddressFilledInApiTokenMode() {
        val state = WelcomeState(credentialType = CredentialType.API_TOKEN)
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonEnabledWhenServerAddressAndTokenNonBlankInApiTokenMode() {
        val state = WelcomeState(credentialType = CredentialType.API_TOKEN)
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.password.setTextAndPlaceCursorAtEnd("my-token")
        assertThat(state.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun switchingToApiTokenEnablesButtonWhenServerAddressAndPasswordFilledWithoutUsername() {
        val state = WelcomeState(credentialType = CredentialType.API_TOKEN)
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.password.setTextAndPlaceCursorAtEnd("my-token")
        assertThat(state.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun switchingBackToCredentialsDisablesButtonWhenUsernameBlank() {
        val state = WelcomeState(credentialType = CredentialType.CREDENTIALS)
        state.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
        state.password.setTextAndPlaceCursorAtEnd("my-token")
        // username deliberately blank
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    // endregion

    // region https:// prepending

    @Test
    fun loginWithPlainDomainPrependsHttpsPrefix() = runTest {
        viewModel.test(this) {
            fillCredentials(serverAddress = "myserver.com")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            awaitState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithExistingHttpsPrefixDoesNotDoublePrepend() = runTest {
        viewModel.test(this) {
            fillCredentials(serverAddress = "https://myserver.com")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            awaitState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithHttpPrefixPassesThroughUnchanged() = runTest {
        viewModel.test(this) {
            fillCredentials(serverAddress = "http://myserver.com")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            awaitState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("http://myserver.com")
    }

    // endregion

    // region Loading state lifecycle

    @Test
    fun isLoadingTrueDuringLoginAndFalseAfterSuccess() = runTest {
        repo.shouldDelayLogin = true
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            assertThat(awaitState().isLoading).isTrue()
            repo.releaseLogin()
            assertThat(awaitState().isLoading).isFalse()
        }
    }

    @Test
    fun isLoadingTrueDuringLoginAndFalseAfterFailure() = runTest {
        repo.shouldDelayLogin = true
        repo.loginResult = LoginError.InvalidCredentials.left()
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            assertThat(awaitState().isLoading).isTrue()
            repo.releaseLogin()
            awaitSideEffect() // LoginFailed side effect is emitted before isLoading=false
            assertThat(awaitState().isLoading).isFalse()
        }
    }

    // endregion

    // region Login success path

    @Test
    fun successfulLoginDoesNotEmitLoginFailedSideEffect() = runTest {
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            awaitState() // isLoading = false — login complete, no side effect emitted
        }
    }

    @Test
    fun credentialsLoginPassesServerAddressUsernameAndPasswordToRepository() = runTest {
        viewModel.test(this) {
            fillCredentials(serverAddress = "server.com", username = "admin", password = "secret")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            awaitState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastCredentialsLoginCall?.second).isEqualTo("admin")
        assertThat(repo.lastCredentialsLoginCall?.third).isEqualTo("secret")
    }

    @Test
    fun apiTokenLoginPassesServerAddressAndTokenToRepository() = runTest {
        viewModel.test(this) {
            val s = viewModel.container.stateFlow.value
            s.serverAddress.setTextAndPlaceCursorAtEnd("server.com")
            s.password.setTextAndPlaceCursorAtEnd("my-api-token")
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
            awaitState() // credentialType change
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            awaitState() // isLoading = false
        }
        assertThat(repo.lastApiTokenLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastApiTokenLoginCall?.second).isEqualTo("my-api-token")
    }

    // endregion

    // region Error side effects

    @Test
    fun invalidCredentialsErrorEmitsLoginFailedWithLoginFailedResource() = runTest {
        repo.loginResult = LoginError.InvalidCredentials.left()
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            val sideEffect = awaitSideEffect()
            assertThat(sideEffect).isInstanceOf(WelcomeSideEffect.LoginFailed::class)
            assertThat((sideEffect as WelcomeSideEffect.LoginFailed).message)
                .isEqualTo(Res.string.login_failed)
            awaitState() // isLoading = false
        }
    }

    @Test
    fun networkErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.NetworkError.left()
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            val sideEffect = awaitSideEffect() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(Res.string.unreachable_server_address)
            awaitState() // isLoading = false
        }
    }

    @Test
    fun unresolvedAddressErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.UnresolvedAddress.left()
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            val sideEffect = awaitSideEffect() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(Res.string.unreachable_server_address)
            awaitState() // isLoading = false
        }
    }

    @Test
    fun unknownErrorEmitsLoginFailedWithSomethingWentWrongResource() = runTest {
        repo.loginResult = LoginError.UnknownError.left()
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitState() // isLoading = true
            val sideEffect = awaitSideEffect() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(Res.string.something_went_wrong)
            awaitState() // isLoading = false
        }
    }

    // endregion

    // region Double-submit guard

    @Test
    fun secondLoginClickWhileInFlightDoesNotTriggerSecondRepositoryCall() = runTest {
        repo.shouldDelayLogin = true
        viewModel.test(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked) // first click
            viewModel.onEvent(WelcomeIntent.OnLoginClicked) // second click — isLoading=true, ignored
            awaitState() // isLoading = true
            repo.releaseLogin()
            awaitState() // isLoading = false
        }
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
