import SwiftUI
import Shared

/// Recipes tab shell, replacing the Compose Navigation3 graph in recipes/presentation/RecipesContent.kt.
/// iPhone: NavigationStack push navigation. iPad: NavigationSplitView list-detail, mirroring the
/// ListDetailSceneStrategy behaviour.
struct RecipesTabView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    var body: some View {
        if horizontalSizeClass == .regular {
            RecipesSplitView()
        } else {
            RecipesStackView()
        }
    }
}

private struct RecipesStackView: View {
    @Environment(TabAccessoryModel.self) private var accessory
    @State private var path: [AppRoute] = []
    @State private var cookingSession: CookingSession?

    var body: some View {
        NavigationStack(path: $path) {
            RecipeListRoot(
                onRecipeTapped: { path.append(.detail(recipeId: $0.id)) },
                onSearchTapped: { path.append(.search) }
            )
            .navigationDestination(for: AppRoute.self) { route in
                switch route {
                case .detail(let recipeId):
                    RecipeDetailsRoot(
                        recipeId: recipeId,
                        onNavigateToComments: { path.append(.comments(recipeId: $0)) }
                    )
                case .comments(let recipeId):
                    RecipeCommentsRoot(recipeId: recipeId)
                case .search:
                    RecipeSearchRoot(
                        onRecipeTapped: { path.append(.detail(recipeId: $0)) }
                    )
                }
            }
        }
        .publishesStartCooking(for: cookableRecipeId, to: accessory) { cookingSession = CookingSession(recipeId: $0) }
        .fullScreenCover(item: $cookingSession) { session in
            CookModeRoot(recipeId: session.recipeId, onExit: { cookingSession = nil })
        }
    }

    /// The recipe list is the stack's root, so a detail screen is on top exactly when the last
    /// pushed route is one — Comments and Search push it out of view.
    private var cookableRecipeId: String? {
        if case .detail(let recipeId) = path.last {
            return recipeId
        }
        return nil
    }
}

private struct RecipesSplitView: View {
    @Environment(TabAccessoryModel.self) private var accessory
    @State private var selectedRecipeId: String?
    @State private var detailPath: [AppRoute] = []
    @State private var cookingSession: CookingSession?

    var body: some View {
        NavigationSplitView {
            RecipeListRoot(
                onRecipeTapped: { recipe in
                    detailPath = []
                    selectedRecipeId = recipe.id
                },
                onSearchTapped: {
                    detailPath = []
                    selectedRecipeId = nil
                }
            )
        } detail: {
            if let recipeId = selectedRecipeId {
                NavigationStack(path: $detailPath) {
                    RecipeDetailsRoot(
                        recipeId: recipeId,
                        onNavigateToComments: { detailPath.append(.comments(recipeId: $0)) }
                    )
                    .id(recipeId)
                    .navigationDestination(for: AppRoute.self) { route in
                        switch route {
                        case .comments(let commentsRecipeId):
                            RecipeCommentsRoot(recipeId: commentsRecipeId)
                        case .detail(let detailRecipeId):
                            RecipeDetailsRoot(
                                recipeId: detailRecipeId,
                                onNavigateToComments: { detailPath.append(.comments(recipeId: $0)) }
                            )
                        case .search:
                            RecipeSearchRoot(
                                onRecipeTapped: { detailPath.append(.detail(recipeId: $0)) }
                            )
                        }
                    }
                }
            } else {
                ContentUnavailableView(
                    "Select a recipe to see details",
                    systemImage: "book.closed"
                )
            }
        }
        .publishesStartCooking(for: cookableRecipeId, to: accessory) { cookingSession = CookingSession(recipeId: $0) }
        .fullScreenCover(item: $cookingSession) { session in
            CookModeRoot(recipeId: session.recipeId, onExit: { cookingSession = nil })
        }
    }

    /// Here the detail column's root *is* a recipe detail, so an empty `detailPath` still counts —
    /// only Comments and Search push it out of view.
    private var cookableRecipeId: String? {
        switch detailPath.last {
        case .none: return selectedRecipeId
        case .some(.detail(let recipeId)): return recipeId
        default: return nil
        }
    }
}

/// `.fullScreenCover(item:)` needs `Identifiable`, which a bare `String` isn't — this just carries
/// the recipe id along for the cover's lifetime.
private struct CookingSession: Identifiable {
    let recipeId: String
    var id: String { recipeId }
}

private extension View {
    /// Offers the Start Cooking accessory to the tab shell while a recipe's details are on top.
    /// Keyed on the recipe id rather than a flag so moving straight from one recipe to another
    /// still republishes — which matters since the action now actually uses the id.
    func publishesStartCooking(
        for recipeId: String?,
        to accessory: TabAccessoryModel,
        onStart: @escaping (String) -> Void
    ) -> some View {
        onChange(of: recipeId, initial: true) { _, id in
            guard let id else {
                accessory.startCooking = nil
                return
            }
            accessory.startCooking = { onStart(id) }
        }
    }
}
