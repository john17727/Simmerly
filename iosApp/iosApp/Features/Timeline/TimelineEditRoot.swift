import SwiftUI
import PhotosUI
import Shared

@MainActor
@Observable
final class TimelineEditModel {
    private let vm: TimelineEditViewModel
    private(set) var state: TimelineEditState
    var snackbar: SnackbarMessage?
    private let onFinished: () -> Void

    init(recipeId: String, eventId: String, onFinished: @escaping () -> Void) {
        self.onFinished = onFinished
        vm = SimmerlyViewModels.shared.timelineEdit(recipeId: recipeId, eventId: eventId)
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
                case .finished:
                    self?.onFinished()
                }
            }
        )
    }

    func send(_ intent: TimelineEditIntent) {
        vm.onEvent(event: intent)
    }
}

struct TimelineEditRoot: View {
    @State private var model: TimelineEditModel

    init(recipeId: String, eventId: String, onFinished: @escaping () -> Void) {
        _model = State(initialValue: TimelineEditModel(recipeId: recipeId, eventId: eventId, onFinished: onFinished))
    }

    var body: some View {
        TimelineEditView(state: model.state, onEvent: { model.send($0) })
            .snackbar($model.snackbar)
            .task { await model.activate() }
    }
}
