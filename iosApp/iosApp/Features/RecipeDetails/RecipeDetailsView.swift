import SwiftUI
import Shared

struct RecipeDetailsView: View {
    let state: RecipeDetailsState
    let onAddServing: () -> Void
    let onRemoveServing: () -> Void
    let onShowSettings: () -> Void
    let onNavigateToComments: () -> Void

    var body: some View {
        Group {
            if state.loading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if state.error != nil && state.recipe.id.isEmpty {
                ContentUnavailableView(
                    "Couldn't load recipe",
                    systemImage: "exclamationmark.triangle",
                    description: Text(String(localized: "something_went_wrong"))
                )
            } else {
                content
            }
        }
        .navigationTitle(state.recipe.title.localized)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button("Recipe Settings", systemImage: "gearshape", action: onShowSettings)
                    if !state.recipe.settings.disableComments {
                        Button("Comments", systemImage: "bubble.left.and.bubble.right", action: onNavigateToComments)
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                }
            }
        }
    }

    private var content: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                if state.isRefreshing {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                }

                heroImage

                VStack(alignment: .leading, spacing: 8) {
                    Text(state.recipe.title.localized)
                        .font(SimmerlyFont.headlineMedium)
                    RecipeMetaRow(
                        rating: state.recipe.rating?.doubleValue,
                        totalTime: state.recipe.totalTime,
                        prepTime: state.recipe.prepTime,
                        cookTime: state.recipe.performTime
                    )
                    TagRow(tags: state.recipe.tags)
                    if let description = state.recipe.description_?.localized, !description.isEmpty {
                        Text(description)
                            .font(SimmerlyFont.bodyMedium)
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }
                }
                .padding(.horizontal, 16)

                IngredientsSection(
                    recipe: state.recipe,
                    onAddServing: onAddServing,
                    onRemoveServing: onRemoveServing
                )

                InstructionsSection(
                    instructions: state.recipe.instructions,
                    ingredients: state.recipe.ingredients
                )

                if !state.recipe.notes.isEmpty {
                    NotesSection(notes: state.recipe.notes)
                }

                if state.recipe.settings.showNutrition {
                    NutritionSection(nutrition: state.recipe.nutrition)
                }
            }
            .padding(.vertical, 8)
        }
        .background(SimmerlyColor.background)
    }

    private var heroImage: some View {
        RemoteImage(url: state.recipe.image) {
            Rectangle().fill(SimmerlyColor.surfaceContainer)
        }
        .aspectRatio(contentMode: .fill)
        .frame(maxWidth: .infinity)
        .frame(height: 260)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .padding(.horizontal, 16)
    }
}
