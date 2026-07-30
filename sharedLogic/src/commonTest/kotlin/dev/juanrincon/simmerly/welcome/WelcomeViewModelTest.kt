package dev.juanrincon.simmerly.welcome

import arrow.core.left
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.auth.FakeAuthRepository
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.core.presentation.StringKey
import dev.juanrincon.simmerly.core.presentation.UiText
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
import org.orbitmvi.orbit.test.OrbitScopedTestContextInternal
import org.orbitmvi.orbit.test.testWithInternalState
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
        assertThat(state.serverAddress).isEqualTo("")
        assertThat(state.username).isEqualTo("")
        assertThat(state.password).isEqualTo("")
        assertThat(state.credentialType).isEqualTo(CredentialType.CREDENTIALS)
        assertThat(state.isLoading).isFalse()
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    // endregion

    // region Credential type

    @Test
    fun onCredentialTypeChangedCredentialsToApiTokenUpdatesCredentialType() = runTest {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
            assertThat(awaitInternalState().credentialType).isEqualTo(CredentialType.API_TOKEN)
        }
    }

    @Test
    fun onCredentialTypeChangedApiTokenBackToCredentialsUpdatesCredentialType() = runTest {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
            awaitInternalState() // consume API_TOKEN state
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.CREDENTIALS))
            assertThat(awaitInternalState().credentialType).isEqualTo(CredentialType.CREDENTIALS)
        }
    }

    // endregion

    // region Field intents update state

    @Test
    fun onServerAddressChangedUpdatesServerAddress() = runTest {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(WelcomeIntent.OnServerAddressChanged("server.com"))
            assertThat(awaitInternalState().serverAddress).isEqualTo("server.com")
        }
    }

    @Test
    fun onUsernameChangedUpdatesUsername() = runTest {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(WelcomeIntent.OnUsernameChanged("user"))
            assertThat(awaitInternalState().username).isEqualTo("user")
        }
    }

    @Test
    fun onPasswordChangedUpdatesPassword() = runTest {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(WelcomeIntent.OnPasswordChanged("pass"))
            assertThat(awaitInternalState().password).isEqualTo("pass")
        }
    }

    // endregion

    // region isLoginButtonEnabled — CREDENTIALS mode
    // WelcomeState's computed property is tested directly since it only depends on plain fields.

    @Test
    fun loginButtonDisabledWhenOnlyServerAddressFilledInCredentialsMode() {
        val state = WelcomeState(serverAddress = "server.com")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonDisabledWhenServerAddressAndUsernameFilledButPasswordEmpty() {
        val state = WelcomeState(serverAddress = "server.com", username = "user")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonDisabledWhenServerAddressAndPasswordFilledButUsernameEmpty() {
        val state = WelcomeState(serverAddress = "server.com", password = "pass")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonEnabledWhenAllCredentialsFieldsNonBlank() {
        val state = WelcomeState(serverAddress = "server.com", username = "user", password = "pass")
        assertThat(state.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun loginButtonDisabledAgainAfterClearingUsername() {
        val filled = WelcomeState(serverAddress = "server.com", username = "user", password = "pass")
        assertThat(filled.isLoginButtonEnabled).isTrue()
        val cleared = filled.copy(username = "")
        assertThat(cleared.isLoginButtonEnabled).isFalse()
    }

    // endregion

    // region isLoginButtonEnabled — API_TOKEN mode

    @Test
    fun loginButtonDisabledWhenOnlyServerAddressFilledInApiTokenMode() {
        val state = WelcomeState(credentialType = CredentialType.API_TOKEN, serverAddress = "server.com")
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    @Test
    fun loginButtonEnabledWhenServerAddressAndTokenNonBlankInApiTokenMode() {
        val state = WelcomeState(
            credentialType = CredentialType.API_TOKEN,
            serverAddress = "server.com",
            password = "my-token"
        )
        assertThat(state.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun switchingToApiTokenEnablesButtonWhenServerAddressAndPasswordFilledWithoutUsername() {
        val state = WelcomeState(
            credentialType = CredentialType.API_TOKEN,
            serverAddress = "server.com",
            password = "my-token"
        )
        assertThat(state.isLoginButtonEnabled).isTrue()
    }

    @Test
    fun switchingBackToCredentialsDisablesButtonWhenUsernameBlank() {
        val state = WelcomeState(
            credentialType = CredentialType.CREDENTIALS,
            serverAddress = "server.com",
            password = "my-token"
            // username deliberately blank
        )
        assertThat(state.isLoginButtonEnabled).isFalse()
    }

    // endregion

    // region https:// prepending

    @Test
    fun loginWithPlainDomainPrependsHttpsPrefix() = runTest {
        viewModel.testWithInternalState(this) {
            fillCredentials(serverAddress = "myserver.com")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            awaitInternalState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithExistingHttpsPrefixDoesNotDoublePrepend() = runTest {
        viewModel.testWithInternalState(this) {
            fillCredentials(serverAddress = "https://myserver.com")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            awaitInternalState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithHttpPrefixPassesThroughUnchanged() = runTest {
        viewModel.testWithInternalState(this) {
            fillCredentials(serverAddress = "http://myserver.com")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            awaitInternalState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("http://myserver.com")
    }

    // endregion

    // region Loading state lifecycle

    @Test
    fun isLoadingTrueDuringLoginAndFalseAfterSuccess() = runTest {
        repo.shouldDelayLogin = true
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            assertThat(awaitInternalState().isLoading).isTrue()
            repo.releaseLogin()
            assertThat(awaitInternalState().isLoading).isFalse()
        }
    }

    @Test
    fun isLoadingTrueDuringLoginAndFalseAfterFailure() = runTest {
        repo.shouldDelayLogin = true
        repo.loginResult = LoginError.InvalidCredentials.left()
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            assertThat(awaitInternalState().isLoading).isTrue()
            repo.releaseLogin()
            awaitSideEffect() // LoginFailed side effect is emitted before isLoading=false
            assertThat(awaitInternalState().isLoading).isFalse()
        }
    }

    // endregion

    // region Login success path

    @Test
    fun successfulLoginDoesNotEmitLoginFailedSideEffect() = runTest {
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            awaitInternalState() // isLoading = false — login complete, no side effect emitted
        }
    }

    @Test
    fun credentialsLoginPassesServerAddressUsernameAndPasswordToRepository() = runTest {
        viewModel.testWithInternalState(this) {
            fillCredentials(serverAddress = "server.com", username = "admin", password = "secret")
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            awaitInternalState() // isLoading = false
        }
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastCredentialsLoginCall?.second).isEqualTo("admin")
        assertThat(repo.lastCredentialsLoginCall?.third).isEqualTo("secret")
    }

    @Test
    fun apiTokenLoginPassesServerAddressAndTokenToRepository() = runTest {
        viewModel.testWithInternalState(this) {
            viewModel.onEvent(WelcomeIntent.OnServerAddressChanged("server.com"))
            awaitInternalState() // serverAddress change
            viewModel.onEvent(WelcomeIntent.OnPasswordChanged("my-api-token"))
            awaitInternalState() // password change
            viewModel.onEvent(WelcomeIntent.OnCredentialTypeChanged(CredentialType.API_TOKEN))
            awaitInternalState() // credentialType change
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            awaitInternalState() // isLoading = false
        }
        assertThat(repo.lastApiTokenLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastApiTokenLoginCall?.second).isEqualTo("my-api-token")
    }

    // endregion

    // region Error side effects

    @Test
    fun invalidCredentialsErrorEmitsLoginFailedWithLoginFailedResource() = runTest {
        repo.loginResult = LoginError.InvalidCredentials.left()
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            val sideEffect = awaitSideEffect()
            assertThat(sideEffect).isInstanceOf(WelcomeSideEffect.LoginFailed::class)
            assertThat((sideEffect as WelcomeSideEffect.LoginFailed).message)
                .isEqualTo(UiText.Resource(StringKey.LoginFailed))
            awaitInternalState() // isLoading = false
        }
    }

    @Test
    fun networkErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.NetworkError.left()
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            val sideEffect = awaitSideEffect() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(UiText.Resource(StringKey.UnreachableServerAddress))
            awaitInternalState() // isLoading = false
        }
    }

    @Test
    fun unresolvedAddressErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.UnresolvedAddress.left()
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            val sideEffect = awaitSideEffect() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(UiText.Resource(StringKey.UnreachableServerAddress))
            awaitInternalState() // isLoading = false
        }
    }

    @Test
    fun unknownErrorEmitsLoginFailedWithSomethingWentWrongResource() = runTest {
        repo.loginResult = LoginError.UnknownError.left()
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked)
            awaitInternalState() // isLoading = true
            val sideEffect = awaitSideEffect() as WelcomeSideEffect.LoginFailed
            assertThat(sideEffect.message).isEqualTo(UiText.Resource(StringKey.SomethingWentWrong))
            awaitInternalState() // isLoading = false
        }
    }

    // endregion

    // region Double-submit guard

    @Test
    fun secondLoginClickWhileInFlightDoesNotTriggerSecondRepositoryCall() = runTest {
        repo.shouldDelayLogin = true
        viewModel.testWithInternalState(this) {
            fillCredentials()
            viewModel.onEvent(WelcomeIntent.OnLoginClicked) // first click
            viewModel.onEvent(WelcomeIntent.OnLoginClicked) // second click — isLoading=true, ignored
            awaitInternalState() // isLoading = true
            repo.releaseLogin()
            awaitInternalState() // isLoading = false
        }
        assertThat(repo.loginCallCount).isEqualTo(1)
    }

    // endregion

    // region Helpers

    private suspend fun OrbitScopedTestContextInternal<WelcomeState, WelcomeState, WelcomeSideEffect, WelcomeViewModel>.fillCredentials(
        serverAddress: String = "server.com",
        username: String = "user",
        password: String = "pass"
    ) {
        containerHost.onEvent(WelcomeIntent.OnServerAddressChanged(serverAddress))
        awaitInternalState()
        containerHost.onEvent(WelcomeIntent.OnUsernameChanged(username))
        awaitInternalState()
        containerHost.onEvent(WelcomeIntent.OnPasswordChanged(password))
        awaitInternalState()
    }

    // endregion
}
