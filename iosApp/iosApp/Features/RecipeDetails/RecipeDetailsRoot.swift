import SwiftUI
import Shared

@MainActor
@Observable
final class RecipeDetailsModel {
    private let vm: RecipeDetailsViewModel
    private(set) var state: RecipeDetailsState
    var snackbar: SnackbarMessage?
    private let onNavigateToShoppingList: (String) -> Void
    private let onDeleted: () -> Void

    init(
        recipeId: String,
        onNavigateToShoppingList: @escaping (String) -> Void,
        onDeleted: @escaping () -> Void
    ) {
        self.onNavigateToShoppingList = onNavigateToShoppingList
        self.onDeleted = onDeleted
        vm = SimmerlyViewModels.shared.recipeDetails(recipeId: recipeId)
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
                case .showMessage(let message):
                    snackbar = SnackbarMessage(text: message.message.localized)
                case .recipeDeleted:
                    onDeleted()
                case .itemsAdded(let added):
                    let listId = added.listId
                    snackbar = SnackbarMessage(
                        text: added.message.localized,
                        actionLabel: "View list",
                        action: { [weak self] in self?.onNavigateToShoppingList(listId) }
                    )
                }
            }
        )
    }

    func send(_ intent: RecipeDetailsIntent) {
        vm.onEvent(event: intent)
    }

    func refresh() async {
        // SKIE bridges a Kotlin suspend function as throwing; a failed refresh is reported by the
        // view model itself, so there is nothing more to do here.
        try? await vm.refreshAndAwait()
    }
}

struct RecipeDetailsRoot: View {
    @State private var model: RecipeDetailsModel
    let onNavigateToComments: (String) -> Void
    let onNavigateToTimeline: (String) -> Void

    init(
        recipeId: String,
        onNavigateToComments: @escaping (String) -> Void,
        onNavigateToTimeline: @escaping (String) -> Void,
        onNavigateToShoppingList: @escaping (String) -> Void,
        onDeleted: @escaping () -> Void
    ) {
        _model = State(
            initialValue: RecipeDetailsModel(
                recipeId: recipeId,
                onNavigateToShoppingList: onNavigateToShoppingList,
                onDeleted: onDeleted
            )
        )
        self.onNavigateToComments = onNavigateToComments
        self.onNavigateToTimeline = onNavigateToTimeline
    }

    var body: some View {
        RecipeDetailsView(
            state: model.state,
            onAddServing: { model.send(RecipeDetailsIntentAddServing.shared) },
            onRemoveServing: { model.send(RecipeDetailsIntentRemoveServing.shared) },
            onShowSettings: { model.send(RecipeDetailsIntentShowSettings.shared) },
            onAddToList: { model.send(RecipeDetailsIntentOpenAddToList.shared) },
            onDelete: { model.send(RecipeDetailsIntentRequestDelete.shared) },
            onNavigateToComments: { onNavigateToComments(model.state.recipe.id) },
            onNavigateToTimeline: { onNavigateToTimeline(model.state.recipe.id) },
            onRefresh: { await model.refresh() }
        )
        .snackbar($model.snackbar)
        .confirmationDialog(
            "Delete \(model.state.recipe.title.localized)?",
            isPresented: Binding(
                get: { model.state.showDeleteConfirm },
                set: { isPresented in
                    if !isPresented { model.send(RecipeDetailsIntentDismissDelete.shared) }
                }
            ),
            titleVisibility: .visible
        ) {
            Button("Delete recipe", role: .destructive) {
                model.send(RecipeDetailsIntentConfirmDelete.shared)
            }
        } message: {
            Text("The recipe will be removed for everyone in your household. This can't be undone.")
        }
        .task { await model.activate() }
        .sheet(
            isPresented: Binding(
                get: { model.state.showSettings },
                set: { isPresented in
                    if !isPresented { model.send(RecipeDetailsIntentDismissSettings.shared) }
                }
            )
        ) {
            RecipeSettingsSheet(
                settings: model.state.recipe.settings,
                onSettingChanged: { model.send(RecipeDetailsIntentUpdateSettings(settings: $0)) }
            )
            .presentationDetents([.medium])
        }
        .sheet(
            isPresented: Binding(
                get: { model.state.addToList != nil },
                set: { isPresented in
                    if !isPresented { model.send(RecipeDetailsIntentDismissAddToList.shared) }
                }
            )
        ) {
            if let addToList = model.state.addToList {
                AddToListSheet(
                    sheet: addToList,
                    recipeTitle: model.state.recipe.title.localized,
                    ingredients: model.state.recipe.ingredients,
                    onEvent: model.send
                )
            }
        }
    }
}
