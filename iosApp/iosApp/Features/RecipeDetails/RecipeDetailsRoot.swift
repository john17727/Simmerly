import SwiftUI
import Shared

@MainActor
@Observable
final class RecipeDetailsModel {
    private let vm: RecipeDetailsViewModel
    private(set) var state: RecipeDetailsState
    var snackbar: SnackbarMessage?
    private let onNavigateToShoppingList: (String) -> Void

    init(recipeId: String, onNavigateToShoppingList: @escaping (String) -> Void) {
        self.onNavigateToShoppingList = onNavigateToShoppingList
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
}

struct RecipeDetailsRoot: View {
    @State private var model: RecipeDetailsModel
    let onNavigateToComments: (String) -> Void

    init(
        recipeId: String,
        onNavigateToComments: @escaping (String) -> Void,
        onNavigateToShoppingList: @escaping (String) -> Void
    ) {
        _model = State(
            initialValue: RecipeDetailsModel(
                recipeId: recipeId,
                onNavigateToShoppingList: onNavigateToShoppingList
            )
        )
        self.onNavigateToComments = onNavigateToComments
    }

    var body: some View {
        RecipeDetailsView(
            state: model.state,
            onAddServing: { model.send(RecipeDetailsIntentAddServing.shared) },
            onRemoveServing: { model.send(RecipeDetailsIntentRemoveServing.shared) },
            onShowSettings: { model.send(RecipeDetailsIntentShowSettings.shared) },
            onAddToList: { model.send(RecipeDetailsIntentOpenAddToList.shared) },
            onNavigateToComments: { onNavigateToComments(model.state.recipe.id) }
        )
        .snackbar($model.snackbar)
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
