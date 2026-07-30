import SwiftUI
import Shared

@MainActor
@Observable
final class RecipeCommentsModel {
    private let vm: RecipeCommentsViewModel
    private(set) var state: RecipeCommentsState

    init(recipeId: String) {
        vm = SimmerlyViewModels.shared.recipeComments(recipeId: recipeId)
        state = vm.stateFlow.value
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        await observeFlow(vm.stateFlow) { [weak self] newState in self?.state = newState }
    }

    func send(_ intent: RecipeCommentsIntent) {
        vm.onEvent(event: intent)
    }
}

struct RecipeCommentsRoot: View {
    @State private var model: RecipeCommentsModel

    init(recipeId: String) {
        _model = State(initialValue: RecipeCommentsModel(recipeId: recipeId))
    }

    var body: some View {
        RecipeCommentsView(
            state: model.state,
            commentText: Binding(
                get: { model.state.commentText },
                set: { model.send(RecipeCommentsIntentOnCommentTextChanged(text: $0)) }
            ),
            onSend: { model.send(RecipeCommentsIntentOnSendCommentClicked.shared) }
        )
        .task { await model.activate() }
    }
}
