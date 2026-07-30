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
    @State private var path: [AppRoute] = []

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
    }
}

private struct RecipesSplitView: View {
    @State private var selectedRecipeId: String?
    @State private var detailPath: [AppRoute] = []

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
    }
}
