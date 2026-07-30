import Shared
import SwiftUI

struct RecipeListView: View {
    let recipes: [RecipeSummary]
    let isLoadingMore: Bool
    let onRecipeTapped: (RecipeSummary) -> Void
    let onSearchTapped: () -> Void
    let onLoadMore: () -> Void
    let onRefresh: () async -> Void

    var body: some View {
        List {
            ForEach(recipes, id: \.id) { recipe in
                RecipeCard(recipe: recipe, onTap: { onRecipeTapped(recipe) })
                    .listRowSeparator(.hidden)
                    .listRowInsets(
                        EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16)
                    )
                    .onAppear {
                        if recipe.id == recipes.last?.id {
                            onLoadMore()
                        }
                    }
            }

            if isLoadingMore {
                HStack {
                    Spacer()
                    ProgressView()
                    Spacer()
                }
                .listRowSeparator(.hidden)
            }
        }
        .listStyle(.plain)
        .refreshable { await onRefresh() }
        .navigationTitle("Recipes")
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Button(action: onSearchTapped) {
                    Image(systemName: "magnifyingglass")
                }
            }
        }
    }
}

// MARK: - Previews

private let recipes = [
    RecipeSummary(
        id: "1",
        name: "Recipe 1",
        image: "",
        tags: [],
        rating: 4.5,
        totalTime: "1 hr 20 min",
        prepTime: "20 min",
        performTime: "1 hr",
        description: "Recip1 is delicious"
    ),
    RecipeSummary(
        id: "2",
        name: "Recipe 2",
        image: "",
        tags: [],
        rating: 5.0,
        totalTime: "1 hr 20 min",
        prepTime: "20 min",
        performTime: "1 hr",
        description: "Recip1 is delicious"
    ),
]

#Preview {
    NavigationStack {
        RecipeListView(
            recipes: recipes,
            isLoadingMore: false,
            onRecipeTapped: { _ in },
            onSearchTapped: {},
            onLoadMore: {},
            onRefresh: {}
        )
    }
}
