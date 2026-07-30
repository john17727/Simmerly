import SwiftUI
import Shared

@MainActor
@Observable
final class RecipeDetailsModel {
    private let vm: RecipeDetailsViewModel
    private(set) var state: RecipeDetailsState

    init(recipeId: String) {
        vm = SimmerlyViewModels.shared.recipeDetails(recipeId: recipeId)
        state = vm.stateFlow.value
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        await observeFlow(vm.stateFlow) { [weak self] newState in self?.state = newState }
    }

    func send(_ intent: RecipeDetailsIntent) {
        vm.onEvent(event: intent)
    }
}

struct RecipeDetailsRoot: View {
    @State private var model: RecipeDetailsModel
    let onNavigateToComments: (String) -> Void

    init(recipeId: String, onNavigateToComments: @escaping (String) -> Void) {
        _model = State(initialValue: RecipeDetailsModel(recipeId: recipeId))
        self.onNavigateToComments = onNavigateToComments
    }

    var body: some View {
        RecipeDetailsView(
            state: model.state,
            onAddServing: { model.send(RecipeDetailsIntentAddServing.shared) },
            onRemoveServing: { model.send(RecipeDetailsIntentRemoveServing.shared) },
            onShowSettings: { model.send(RecipeDetailsIntentShowSettings.shared) },
            onNavigateToComments: { onNavigateToComments(model.state.recipe.id) }
        )
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
    }
}
