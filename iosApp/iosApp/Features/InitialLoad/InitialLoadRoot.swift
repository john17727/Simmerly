import SwiftUI
import Shared

@MainActor
@Observable
final class InitialLoadModel {
    private let vm: InitialLoadViewModel

    init() {
        vm = SimmerlyViewModels.shared.initialLoad()
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate(onLoadComplete: @escaping () -> Void) async {
        await observeFlow(vm.events) { _ in onLoadComplete() }
    }
}

struct InitialLoadRoot: View {
    @State private var model = InitialLoadModel()
    let onLoadComplete: () -> Void

    var body: some View {
        VStack(spacing: 32) {
            Image("LaunchLogo")
                .renderingMode(.template)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(minWidth: 100, idealWidth: 125, maxWidth: 150, minHeight: 100, idealHeight: 125, maxHeight: 150)
                .foregroundStyle(SimmerlyColor.primary)
            ProgressView()
                .tint(SimmerlyColor.primary)
                .frame(maxWidth: 200)
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(SimmerlyColor.surface)
        .task { await model.activate(onLoadComplete: onLoadComplete) }
    }
}
