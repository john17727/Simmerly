import SwiftUI
import Shared

@MainActor
@Observable
final class RecipeSearchModel {
    private let vm: RecipeSearchViewModel
    private(set) var state: RecipeSearchState

    init() {
        vm = SimmerlyViewModels.shared.recipeSearch()
        state = vm.stateFlow.value
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        await observeFlow(vm.stateFlow) { [weak self] newState in self?.state = newState }
    }

    func send(_ intent: RecipeSearchIntent) {
        vm.onEvent(event: intent)
    }
}

struct RecipeSearchRoot: View {
    @State private var model = RecipeSearchModel()
    let onRecipeTapped: (String) -> Void

    var body: some View {
        RecipeSearchView(
            state: model.state,
            searchQuery: Binding(
                get: { model.state.searchQuery },
                set: { model.send(RecipeSearchIntentOnQueryChanged(query: $0)) }
            ),
            onSubmit: { model.send(RecipeSearchIntentOnQuerySubmitted(query: model.state.searchQuery)) },
            onRecipeTapped: { recipeId in
                model.send(RecipeSearchIntentOnRecipeViewed(recipeId: recipeId))
                onRecipeTapped(recipeId)
            }
        )
        .task { await model.activate() }
    }
}
