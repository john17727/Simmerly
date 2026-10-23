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

    @Environment(TabAccessoryModel.self) private var accessory
    @State private var model = ShoppingListsModel()
    @FocusState private var searchFocused: Bool

    var body: some View {
        ShoppingListsView(state: model.state, onEvent: model.send)
            .snackbar($model.snackbar)
            .task { await model.activate() }
            // Search and New list sit in the tab view's bottom accessory, which only the TabView can
            // render — so publish them there while this screen is on top, and withdraw on the way out.
            .onAppear(perform: publishAccessory)
            .onChange(of: model.state.query) { publishAccessory() }
            .onDisappear {
                accessory.shoppingLists = nil
                accessory.isEditingSearch = false
            }
            // The accessory's search field is only a stand-in, since the keyboard covers it:
            // the real one shows here, riding just above the keyboard, until it loses focus.
            .safeAreaInset(edge: .bottom, spacing: 0) {
                if accessory.isEditingSearch {
                    SearchEditingBar(
                        query: Binding(
                            get: { model.state.query },
                            set: { model.send(ShoppingListsIntentOnQueryChanged(query: $0)) }
                        ),
                        focused: $searchFocused
                    )
                    .onAppear { searchFocused = true }
                }
            }
            .onChange(of: searchFocused) { _, isFocused in
                if !isFocused { accessory.isEditingSearch = false }
            }
            .onChange(of: model.listToOpen) { _, listId in
                guard let listId else { return }
                model.listToOpen = nil
                onListSelected(listId)
            }
    }

    private func publishAccessory() {
        accessory.shoppingLists = ShoppingListsAccessoryContent(
            query: model.state.query,
            onQueryChanged: { [model] in model.send(ShoppingListsIntentOnQueryChanged(query: $0)) },
            onNewList: { [model] in model.send(ShoppingListsIntentOnNewListClicked.shared) }
        )
    }
}
