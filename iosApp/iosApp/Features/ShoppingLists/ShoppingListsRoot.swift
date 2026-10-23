import SwiftUI
import Shared

/// Bridges `ShoppingListsViewModel` into SwiftUI, mirroring shoppinglists/presentation/list/ShoppingListsScreen.kt.
@MainActor
@Observable
final class ShoppingListsModel {
    private let vm: ShoppingListsViewModel
    private(set) var state: ShoppingListsState
    var snackbar: SnackbarMessage?
    /// The list the ViewModel asked to open; the Root hands it to navigation and clears it.
    var listToOpen: String?

    init() {
        vm = SimmerlyViewModels.shared.shoppingLists()
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
                case .openList(let open):
                    self?.listToOpen = open.listId
                case .showMessage(let message):
                    self?.snackbar = SnackbarMessage(text: message.message.localized)
                }
            }
        )
    }

    func send(_ intent: ShoppingListsIntent) {
        vm.onEvent(event: intent)
    }
}

struct ShoppingListsRoot: View {
    let onListSelected: (String) -> Void

    @State private var model = ShoppingListsModel()

    var body: some View {
        ShoppingListsView(
            state: model.state,
            query: Binding(
                get: { model.state.query },
                set: { model.send(ShoppingListsIntentOnQueryChanged(query: $0)) }
            ),
            onEvent: model.send
        )
        .snackbar($model.snackbar)
        .task { await model.activate() }
        .onChange(of: model.listToOpen) { _, listId in
            guard let listId else { return }
            model.listToOpen = nil
            onListSelected(listId)
        }
    }
}
