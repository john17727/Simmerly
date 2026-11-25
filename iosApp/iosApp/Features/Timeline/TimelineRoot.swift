import SwiftUI
import Shared

@MainActor
@Observable
final class TimelineModel {
    private let vm: TimelineViewModel
    private(set) var state: TimelineState
    var snackbar: SnackbarMessage?

    init(recipeId: String) {
        vm = SimmerlyViewModels.shared.timeline(recipeId: recipeId)
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
                case .showMessage(let message):
                    self?.snackbar = SnackbarMessage(text: message.message.localized)
                }
            }
        )
    }

    func send(_ intent: TimelineIntent) {
        vm.onEvent(event: intent)
    }

    func refresh() async {
        // Failures surface through the view model's side effects.
        try? await vm.refreshAndAwait()
    }
}

struct TimelineRoot: View {
    @State private var model: TimelineModel
    let onEdit: (String) -> Void

    init(recipeId: String, onEdit: @escaping (String) -> Void) {
        _model = State(initialValue: TimelineModel(recipeId: recipeId))
        self.onEdit = onEdit
    }

    var body: some View {
        TimelineView(
            state: model.state,
            onEvent: { model.send($0) },
            onEdit: onEdit,
            onRefresh: { await model.refresh() }
        )
        .snackbar($model.snackbar)
        .task { await model.activate() }
    }
}
