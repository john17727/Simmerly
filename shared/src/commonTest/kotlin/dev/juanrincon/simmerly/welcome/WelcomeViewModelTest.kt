package dev.juanrincon.simmerly.welcome

import app.cash.turbine.test
import arrow.core.left
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.arkivanov.mvikotlin.main.store.DefaultStoreFactory
import dev.juanrincon.simmerly.auth.FakeAuthRepository
import dev.juanrincon.simmerly.auth.domain.LoginError
import dev.juanrincon.simmerly.welcome.presentation.WelcomeViewModel
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStore
import dev.juanrincon.simmerly.welcome.presentation.mvikotlin.WelcomeStoreFactory
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
        viewModel = WelcomeViewModel(WelcomeStoreFactory(DefaultStoreFactory(), repo))
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initial state

    @Test
    fun initialStateHasEmptyFieldsCredentialsTypeAndLoginButtonDisabled() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertThat(state.serverAddress).isEqualTo("")
            assertThat(state.username).isEqualTo("")
            assertThat(state.password).isEqualTo("")
            assertThat(state.credentialType).isEqualTo(WelcomeStore.CredentialType.CREDENTIALS)
            assertThat(state.isLoading).isFalse()
            assertThat(state.isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Field updates

    @Test
    fun onServerAddressChangedUpdatesServerAddressInState() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("myserver.com"))
            assertThat(awaitItem().serverAddress).isEqualTo("myserver.com")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onUsernameChangedUpdatesUsernameInState() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnUsernameChanged("admin"))
            assertThat(awaitItem().username).isEqualTo("admin")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onPasswordChangedUpdatesPasswordInState() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged("secret"))
            assertThat(awaitItem().password).isEqualTo("secret")
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onCredentialTypeChangedCredentialsToApiTokenUpdatesCredentialType() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN))
            assertThat(awaitItem().credentialType).isEqualTo(WelcomeStore.CredentialType.API_TOKEN)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun onCredentialTypeChangedApiTokenBackToCredentialsUpdatesCredentialType() = runTest {
        viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN))
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.CREDENTIALS))
            assertThat(awaitItem().credentialType).isEqualTo(WelcomeStore.CredentialType.CREDENTIALS)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region isLoginButtonEnabled — CREDENTIALS mode

    @Test
    fun loginButtonDisabledWhenOnlyServerAddressFilledInCredentialsMode() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
            assertThat(awaitItem().isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loginButtonDisabledWhenServerAddressAndUsernameFilledButPasswordEmpty() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnUsernameChanged("user"))
            assertThat(awaitItem().isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loginButtonDisabledWhenServerAddressAndPasswordFilledButUsernameEmpty() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged("pass"))
            assertThat(awaitItem().isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loginButtonEnabledWhenAllCredentialsFieldsNonBlank() = runTest {
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnUsernameChanged("user"))
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged("pass"))
            assertThat(awaitItem().isLoginButtonEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loginButtonDisabledAgainAfterClearingUsername() = runTest {
        fillCredentials()
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnUsernameChanged(""))
            assertThat(awaitItem().isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region isLoginButtonEnabled — API_TOKEN mode

    @Test
    fun loginButtonDisabledWhenOnlyServerAddressFilledInApiTokenMode() = runTest {
        viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN))
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
            assertThat(awaitItem().isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loginButtonEnabledWhenServerAddressAndTokenNonBlankInApiTokenMode() = runTest {
        viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN))
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged("my-token"))
            assertThat(awaitItem().isLoginButtonEnabled).isTrue()
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun switchingToApiTokenEnablesButtonWhenServerAddressAndPasswordFilledWithoutUsername() =
        runTest {
            viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
            viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged("my-token"))
            viewModel.state.test {
                skipItems(1)
                viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN))
                assertThat(awaitItem().isLoginButtonEnabled).isTrue()
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun switchingBackToCredentialsDisablesButtonWhenUsernameBlank() = runTest {
        viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
        viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged("my-token"))
        viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN))
        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.CREDENTIALS))
            assertThat(awaitItem().isLoginButtonEnabled).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region https:// prepending

    @Test
    fun loginWithPlainDomainPrependsHttpsPrefix() = runTest {
        fillCredentials(serverAddress = "myserver.com")
        viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithExistingHttpsPrefixDoesNotDoublePrepend() = runTest {
        fillCredentials(serverAddress = "https://myserver.com")
        viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://myserver.com")
    }

    @Test
    fun loginWithHttpPrefixPassesThroughUnchanged() = runTest {
        fillCredentials(serverAddress = "http://myserver.com")
        viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("http://myserver.com")
    }

    // endregion

    // region Loading state lifecycle

    @Test
    fun isLoadingTrueDuringLoginAndFalseAfterSuccess() = runTest {
        repo.shouldDelayLogin = true
        fillCredentials()

        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
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

        viewModel.state.test {
            skipItems(1)
            viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
            assertThat(awaitItem().isLoading).isTrue()
            repo.releaseLogin()
            assertThat(awaitItem().isLoading).isFalse()
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Login success path

    @Test
    fun successfulLoginDoesNotEmitLoginFailedLabel() = runTest {
        fillCredentials()
        viewModel.labels.test {
            viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
            expectNoEvents()
        }
    }

    @Test
    fun credentialsLoginPassesServerAddressUsernameAndPasswordToRepository() = runTest {
        fillCredentials(serverAddress = "server.com", username = "admin", password = "secret")
        viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
        assertThat(repo.lastCredentialsLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastCredentialsLoginCall?.second).isEqualTo("admin")
        assertThat(repo.lastCredentialsLoginCall?.third).isEqualTo("secret")
    }

    @Test
    fun apiTokenLoginPassesServerAddressAndTokenToRepository() = runTest {
        viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged("server.com"))
        viewModel.onEvent(WelcomeStore.Intent.OnCredentialTypeChanged(WelcomeStore.CredentialType.API_TOKEN))
        viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged("my-api-token"))
        viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
        assertThat(repo.lastApiTokenLoginCall?.first).isEqualTo("https://server.com")
        assertThat(repo.lastApiTokenLoginCall?.second).isEqualTo("my-api-token")
    }

    // endregion

    // region Error labels

    @Test
    fun invalidCredentialsErrorEmitsLoginFailedWithLoginFailedResource() = runTest {
        repo.loginResult = LoginError.InvalidCredentials.left()
        fillCredentials()
        viewModel.labels.test {
            viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
            val label = awaitItem()
            assertThat(label).isInstanceOf(WelcomeStore.Label.LoginFailed::class)
            assertThat((label as WelcomeStore.Label.LoginFailed).message)
                .isEqualTo(Res.string.login_failed)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun networkErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.NetworkError.left()
        fillCredentials()
        viewModel.labels.test {
            viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
            val label = awaitItem() as WelcomeStore.Label.LoginFailed
            assertThat(label.message).isEqualTo(Res.string.unreachable_server_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unresolvedAddressErrorEmitsLoginFailedWithUnreachableServerAddressResource() = runTest {
        repo.loginResult = LoginError.UnresolvedAddress.left()
        fillCredentials()
        viewModel.labels.test {
            viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
            val label = awaitItem() as WelcomeStore.Label.LoginFailed
            assertThat(label.message).isEqualTo(Res.string.unreachable_server_address)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun unknownErrorEmitsLoginFailedWithSomethingWentWrongResource() = runTest {
        repo.loginResult = LoginError.UnknownError.left()
        fillCredentials()
        viewModel.labels.test {
            viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
            val label = awaitItem() as WelcomeStore.Label.LoginFailed
            assertThat(label.message).isEqualTo(Res.string.something_went_wrong)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Double-submit guard

    @Test
    fun secondLoginClickWhileInFlightDoesNotTriggerSecondRepositoryCall() = runTest {
        repo.shouldDelayLogin = true
        fillCredentials()
        viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
        viewModel.onEvent(WelcomeStore.Intent.OnLoginClicked)
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
        viewModel.onEvent(WelcomeStore.Intent.OnServerAddressChanged(serverAddress))
        viewModel.onEvent(WelcomeStore.Intent.OnUsernameChanged(username))
        viewModel.onEvent(WelcomeStore.Intent.OnPasswordChanged(password))
    }

    // endregion
}
