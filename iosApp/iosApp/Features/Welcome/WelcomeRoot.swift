import SwiftUI
import Shared

@MainActor
@Observable
final class WelcomeModel {
    private let vm: WelcomeViewModel
    private(set) var state: WelcomeState
    var loginFailedMessage: String?

    init() {
        vm = SimmerlyViewModels.shared.welcome()
        state = vm.stateFlow.value
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        await observe(
            state: vm.stateFlow,
            onState: { [weak self] newState in self?.state = newState },
            sideEffects: vm.sideEffectFlow,
            onSideEffect: { [weak self] effect in
                switch onEnum(of: effect) {
                case .loginFailed(let failed):
                    self?.loginFailedMessage = failed.message.localized
                }
            }
        )
    }

    func send(_ intent: WelcomeIntent) {
        vm.onEvent(event: intent)
    }
}

struct WelcomeRoot: View {
    @State private var model = WelcomeModel()

    var body: some View {
        WelcomeView(
            state: model.state,
            serverAddress: Binding(
                get: { model.state.serverAddress },
                set: { model.send(WelcomeIntentOnServerAddressChanged(value: $0)) }
            ),
            username: Binding(
                get: { model.state.username },
                set: { model.send(WelcomeIntentOnUsernameChanged(value: $0)) }
            ),
            password: Binding(
                get: { model.state.password },
                set: { model.send(WelcomeIntentOnPasswordChanged(value: $0)) }
            ),
            onCredentialTypeChanged: { model.send(WelcomeIntentOnCredentialTypeChanged(credentialType: $0)) },
            onLogin: { model.send(WelcomeIntentOnLoginClicked.shared) }
        )
        .task { await model.activate() }
        .alert(
            "Login failed",
            isPresented: Binding(
                get: { model.loginFailedMessage != nil },
                set: { isPresented in if !isPresented { model.loginFailedMessage = nil } }
            )
        ) {
            Button("OK") { model.loginFailedMessage = nil }
        } message: {
            Text(model.loginFailedMessage ?? "")
        }
    }
}
