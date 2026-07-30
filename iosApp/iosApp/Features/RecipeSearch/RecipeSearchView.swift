import SwiftUI
import Shared

struct RecipeSearchView: View {
    let state: RecipeSearchState
    @Binding var searchQuery: String
    let onSubmit: () -> Void
    let onRecipeTapped: (String) -> Void

    private var suggestions: [RecipeSummary] {
        guard !state.searchQuery.isEmpty else { return [] }
        return state.recipes.filter { $0.name.localizedCaseInsensitiveContains(state.searchQuery) }
    }

    private var results: [RecipeSummary] {
        guard !state.submittedQuery.isEmpty else { return [] }
        return state.recipes.filter { $0.name.localizedCaseInsensitiveContains(state.submittedQuery) }
    }

    var body: some View {
        List {
            if state.submittedQuery.isEmpty {
                if !state.recentRecipes.isEmpty {
                    Section("Recently Viewed") {
                        recipeRows(state.recentRecipes)
                    }
                }
            } else if state.isLoading && results.isEmpty {
                ForEach(0..<6, id: \.self) { _ in
                    RecipeCardSkeleton()
                        .listRowSeparator(.hidden)
                }
            } else {
                recipeRows(results)
            }
        }
        .listStyle(.plain)
        .navigationTitle("Search")
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $searchQuery, placement: .navigationBarDrawer(displayMode: .always))
        .searchSuggestions {
            if state.searchQuery.isEmpty {
                ForEach(state.recentQueries, id: \.self) { query in
                    Label(query, systemImage: "clock.arrow.circlepath")
                        .searchCompletion(query)
                }
            } else {
                ForEach(suggestions, id: \.id) { recipe in
                    Text(recipe.name)
                        .searchCompletion(recipe.name)
                }
            }
        }
        .onSubmit(of: .search, onSubmit)
    }

    @ViewBuilder
    private func recipeRows(_ recipes: [RecipeSummary]) -> some View {
        ForEach(recipes, id: \.id) { recipe in
            RecipeCard(recipe: recipe, onTap: { onRecipeTapped(recipe.id) })
                .listRowSeparator(.hidden)
                .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
        }
    }
}
