import SwiftUI
import Shared

@MainActor
@Observable
final class RootModel {
    private let vm: AuthNavigationViewModel
    private(set) var route: AuthRoute

    init() {
        vm = SimmerlyViewModels.shared.authNavigation()
        route = vm.isAuthenticated.value
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        await observeFlow(vm.isAuthenticated) { [weak self] route in
            self?.route = route
        }
    }
}

struct RootView: View {
    @State private var model = RootModel()
    // InitialLoad -> App isn't driven by AuthRoute (AuthNavigationViewModel maps Authenticated
    // to .initialLoad, never .app) — it's a one-shot transition triggered by InitialLoadViewModel
    // finishing, mirroring the onLoadComplete callback in the Compose nav graph.
    @State private var initialLoadComplete = false

    var body: some View {
        Group {
            switch model.route {
            case .splash:
                SplashView()
            case .login:
                WelcomeRoot()
            case .initialLoad:
                if initialLoadComplete {
                    MainView()
                } else {
                    InitialLoadRoot(onLoadComplete: { initialLoadComplete = true })
                }
            case .app:
                MainView()
            }
        }
        .task { await model.activate() }
        .onChange(of: model.route) { _, newRoute in
            if newRoute != .initialLoad {
                initialLoadComplete = false
            }
        }
    }
}
