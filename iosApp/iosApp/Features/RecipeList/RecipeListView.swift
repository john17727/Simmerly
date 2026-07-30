import SwiftUI
import Shared

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
                    .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
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
