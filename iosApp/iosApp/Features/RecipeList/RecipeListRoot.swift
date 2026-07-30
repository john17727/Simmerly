import SwiftUI
import Shared

@MainActor
@Observable
final class RecipeListModel {
    let vm: RecipeListViewModel
    private(set) var recipes: [RecipeSummary] = []
    private(set) var isLoadingMore = false
    private(set) var endReached = false

    init() {
        vm = SimmerlyViewModels.shared.recipeList()
        recipes = vm.pagedRecipes.value
        isLoadingMore = vm.isLoadingMore.value.boolValue
        endReached = vm.endReached.value.boolValue
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        let recipesFlow = vm.pagedRecipes
        let loadingFlow = vm.isLoadingMore
        let endFlow = vm.endReached
        let onRecipes: ([RecipeSummary]) -> Void = { [weak self] in self?.recipes = $0 }
        let onLoading: (KotlinBoolean) -> Void = { [weak self] in self?.isLoadingMore = $0.boolValue }
        let onEnd: (KotlinBoolean) -> Void = { [weak self] in self?.endReached = $0.boolValue }
        await withTaskGroup(of: Void.self) { group in
            group.addTask { await observeFlow(recipesFlow, onEach: onRecipes) }
            group.addTask { await observeFlow(loadingFlow, onEach: onLoading) }
            group.addTask { await observeFlow(endFlow, onEach: onEnd) }
        }
    }

    func loadNextPage() {
        vm.loadNextPage()
    }

    func refresh() async {
        vm.refresh()
    }
}

struct RecipeListRoot: View {
    @State private var model = RecipeListModel()
    let onRecipeTapped: (RecipeSummary) -> Void
    let onSearchTapped: () -> Void

    var body: some View {
        RecipeListView(
            recipes: model.recipes,
            isLoadingMore: model.isLoadingMore,
            onRecipeTapped: onRecipeTapped,
            onSearchTapped: onSearchTapped,
            onLoadMore: { model.loadNextPage() },
            onRefresh: { await model.refresh() }
        )
        .task { await model.activate() }
    }
}
