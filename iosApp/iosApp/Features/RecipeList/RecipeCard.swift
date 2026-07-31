import Shared
import SwiftUI

struct RecipeCard: View {
    let recipe: RecipeSummary
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .center, spacing: 16) {
                RemoteImage(
                    url: recipe.image,
                    targetSize: CGSize(width: 100, height: 100)
                ) {
                    RoundedRectangle(cornerRadius: 8).fill(
                        SimmerlyColor.surfaceContainer
                    )
                }
                .frame(width: 100, height: 100)
                .clipShape(RoundedRectangle(cornerRadius: 8))

                VStack(alignment: .leading, spacing: 8) {
                    Text(recipe.name)
                        .font(SimmerlyFont.titleMedium)
                        .foregroundStyle(SimmerlyColor.onSurface)
                        .multilineTextAlignment(.leading)

                    RecipeMetaRow(rating: recipe.rating?.doubleValue, totalTime: recipe.totalTime, prepTime: nil, cookTime: nil)

                    TagRow(tags: recipe.tags)
                }

                Spacer(minLength: 0)
            }
            .padding(10)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .background(
            SimmerlyColor.surface,
            in: RoundedRectangle(cornerRadius: 12)
        )
        .overlay(
            RoundedRectangle(cornerRadius: 12).strokeBorder(
                SimmerlyColor.outlineVariant,
                lineWidth: 1
            )
        )
    }
}

struct RecipeCardSkeleton: View {
    var body: some View {
        HStack(spacing: 16) {
            RoundedRectangle(cornerRadius: 8)
                .fill(SimmerlyColor.surfaceContainer)
                .frame(width: 100, height: 100)
            VStack(alignment: .leading, spacing: 8) {
                RoundedRectangle(cornerRadius: 4)
                    .fill(SimmerlyColor.surfaceContainer)
                    .frame(height: 18)
                RoundedRectangle(cornerRadius: 4)
                    .fill(SimmerlyColor.surfaceContainer)
                    .frame(width: 80, height: 14)
            }
        }
        .padding(10)
        .redacted(reason: .placeholder)
    }
}

// MARK: - Previews

private let recipe =
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
    )

#Preview {
    RecipeCard(recipe: recipe, onTap: {})
}
