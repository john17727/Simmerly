import SwiftUI
import Shared

/// Bridges `ShoppingListDetailViewModel` into SwiftUI, mirroring
/// shoppinglists/presentation/detail/ShoppingListDetailScreen.kt.
@MainActor
@Observable
final class ShoppingListDetailModel {
    private let vm: ShoppingListDetailViewModel
    private(set) var state: ShoppingListDetailState
    var snackbar: SnackbarMessage?
    private(set) var didNavigateBack = false

    init(listId: String) {
        vm = SimmerlyViewModels.shared.shoppingListDetail(listId: listId)
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
                guard let self else { return }
                switch onEnum(of: effect) {
                case .navigateBack:
                    didNavigateBack = true
                case .showMessage(let message):
                    snackbar = SnackbarMessage(text: message.message.localized)
                case .showCheckedAll(let checkedAll):
                    snackbar = SnackbarMessage(
                        text: checkedAll.count == 1 ? "1 item checked" : "\(checkedAll.count) items checked",
                        actionLabel: "Undo",
                        action: { [weak self] in self?.send(ShoppingListDetailIntentOnUndoCheckAllClicked.shared) }
                    )
                }
            }
        )
    }

    func send(_ intent: ShoppingListDetailIntent) {
        vm.onEvent(event: intent)
    }
}

struct ShoppingListDetailRoot: View {
    let onNavigateBack: () -> Void

    @State private var model: ShoppingListDetailModel

    init(listId: String, onNavigateBack: @escaping () -> Void) {
        self.onNavigateBack = onNavigateBack
        _model = State(initialValue: ShoppingListDetailModel(listId: listId))
    }

    var body: some View {
        ShoppingListDetailView(
            state: model.state,
            addText: Binding(
                get: { model.state.addText },
                set: { model.send(ShoppingListDetailIntentOnAddTextChanged(text: $0)) }
            ),
            snackbar: $model.snackbar,
            onEvent: model.send
        )
        .task { await model.activate() }
        .onChange(of: model.didNavigateBack) { _, didNavigate in
            if didNavigate { onNavigateBack() }
        }
        .sheet(
            isPresented: Binding(
                get: { model.state.editor != nil },
                set: { isPresented in
                    if !isPresented { model.send(ShoppingListDetailIntentOnEditorDismissed.shared) }
                }
            )
        ) {
            if let editor = model.state.editor {
                EditItemSheet(
                    editor: editor,
                    units: model.state.units,
                    foodSuggestions: model.state.foodSuggestions,
                    onEvent: model.send
                )
            }
        }
    }
}
