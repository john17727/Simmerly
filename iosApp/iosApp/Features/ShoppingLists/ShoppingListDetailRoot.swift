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

    @Environment(TabAccessoryModel.self) private var accessory
    @State private var model: ShoppingListDetailModel
    @FocusState private var addFieldFocused: Bool

    init(listId: String, onNavigateBack: @escaping () -> Void) {
        self.onNavigateBack = onNavigateBack
        _model = State(initialValue: ShoppingListDetailModel(listId: listId))
    }

    var body: some View {
        ShoppingListDetailView(
            state: model.state,
            addFieldFocused: accessory.isEditingAddItem,
            snackbar: $model.snackbar,
            onEvent: model.send
        )
        .task { await model.activate() }
        // The add-item field sits in the tab view's bottom accessory, like Start Cooking on recipe
        // details, so publish it there while this list is on top.
        .onAppear(perform: publishAccessory)
        .onChange(of: model.state.addText) { publishAccessory() }
        .onChange(of: model.state.isAdding) { publishAccessory() }
        .onDisappear {
            accessory.addItem = nil
            accessory.isEditingAddItem = false
        }
        // The accessory's add-item field is only a stand-in, since the keyboard covers it: the
        // real one shows here, riding just above the keyboard with the matching ingredients
        // above it, until it loses focus.
        .safeAreaInset(edge: .bottom, spacing: 0) {
            if accessory.isEditingAddItem {
                VStack(spacing: 0) {
                    if addFieldFocused && !model.state.addSuggestions.isEmpty {
                        AddItemSuggestions(
                            suggestions: model.state.addSuggestions,
                            query: model.state.addSuggestionQuery,
                            highlightedId: model.state.addHighlightedId,
                            onPicked: { model.send(ShoppingListDetailIntentOnAddSuggestionPicked(food: $0)) }
                        )
                        .padding(.horizontal, 16)
                        .padding(.bottom, 8)
                    }
                    AddItemBar(
                        text: Binding(
                            get: { model.state.addText },
                            set: { model.send(ShoppingListDetailIntentOnAddTextChanged(text: $0)) }
                        ),
                        isAdding: model.state.isAdding,
                        focused: $addFieldFocused,
                        onSubmit: { model.send(ShoppingListDetailIntentOnAddItemSubmitted.shared) }
                    )
                    .onAppear { addFieldFocused = true }
                }
            }
        }
        .onChange(of: addFieldFocused) { _, isFocused in
            if !isFocused { accessory.isEditingAddItem = false }
        }
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

    private func publishAccessory() {
        accessory.addItem = AddItemAccessoryContent(
            text: model.state.addText,
            isAdding: model.state.isAdding,
            onTextChanged: { [model] in model.send(ShoppingListDetailIntentOnAddTextChanged(text: $0)) },
            onSubmit: { [model] in model.send(ShoppingListDetailIntentOnAddItemSubmitted.shared) }
        )
    }
}
