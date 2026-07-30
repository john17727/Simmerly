import SwiftUI
import Shared

struct RecipeCard: View {
    let recipe: RecipeSummary
    let onTap: () -> Void

    var body: some View {
        Button(action: onTap) {
            HStack(alignment: .top, spacing: 16) {
                RemoteImage(url: recipe.image) {
                    RoundedRectangle(cornerRadius: 8).fill(SimmerlyColor.surfaceContainer)
                }
                .aspectRatio(contentMode: .fill)
                .frame(width: 100, height: 100)
                .clipShape(RoundedRectangle(cornerRadius: 8))

                VStack(alignment: .leading, spacing: 8) {
                    Text(recipe.name)
                        .font(SimmerlyFont.titleMedium)
                        .foregroundStyle(SimmerlyColor.onSurface)
                        .multilineTextAlignment(.leading)

                    HStack(spacing: 8) {
                        if recipe.isFavorite {
                            Image(systemName: "heart.fill")
                                .font(.system(size: 12))
                                .foregroundStyle(SimmerlyColor.primary)
                        }
                        if let rating = recipe.rating {
                            Label("\(rating)", systemImage: "star.fill")
                        }
                        if let totalTime = recipe.totalTime {
                            Label(totalTime, systemImage: "timer")
                        }
                    }
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    .labelStyle(.titleAndIcon)

                    TagRow(tags: recipe.tags)
                }

                Spacer(minLength: 0)
            }
            .padding(10)
        }
        .buttonStyle(.plain)
        .background(SimmerlyColor.surface, in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
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
