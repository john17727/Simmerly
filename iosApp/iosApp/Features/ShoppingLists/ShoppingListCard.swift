import SwiftUI
import Shared

/// One list on the lists screen. Mirrors `ShoppingListCard` and its footers in
/// shoppinglists/presentation/list/ShoppingListsScreen.kt.
struct ShoppingListCard: View {
    let list: ShoppingListCardUi
    let onTap: () -> Void
    let onRename: () -> Void
    let onDelete: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top, spacing: 8) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(list.name.localized)
                        .font(SimmerlyFont.titleMedium)
                        .foregroundStyle(SimmerlyColor.onSurface)
                        .lineLimit(2)
                    Text(itemSummary)
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                Menu {
                    Button("Rename", systemImage: "pencil", action: onRename)
                    Button("Delete", systemImage: "trash", role: .destructive, action: onDelete)
                } label: {
                    Image(systemName: "ellipsis")
                        .rotationEffect(.degrees(90))
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        .frame(width: 44, height: 44)
                        .contentShape(Rectangle())
                }
                .accessibilityLabel("More options for \(list.name.localized)")
                .padding(.top, -12)
                .padding(.trailing, -12)
            }
            ListProgressBar(progress: Double(list.progress))
            footer
        }
        .padding(16)
        .background(SimmerlyColor.surfaceContainerLow, in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
        .contentShape(RoundedRectangle(cornerRadius: 12))
        .onTapGesture(perform: onTap)
        .accessibilityElement(children: .contain)
        .accessibilityAddTraits(.isButton)
    }

    private var itemSummary: String {
        let count = Int(list.itemCount)
        let checked = Int(list.checkedCount)
        if count == 0 { return "No items yet" }
        let items = count == 1 ? "1 item" : "\(count) items"
        let checkedText = checked == 0 ? "none checked" : checked == count ? "all checked" : "\(checked) checked"
        return "\(items) · \(checkedText)"
    }

    @ViewBuilder
    private var footer: some View {
        switch onEnum(of: list.footer) {
        case .empty:
            EmptyView()
        case .oneRecipe(let one):
            FooterDivider()
            FooterRow(
                title: one.recipe.name.localized,
                subtitle: one.itemCount == 1 ? "1 item from this recipe" : "All \(one.itemCount) items from this recipe"
            ) {
                RecipeThumbnail(imageUrl: one.recipe.imageUrl, size: 48)
                    .accessibilityLabel("Recipe photo, \(one.recipe.name.localized)")
            } trailing: {
                Text("×\(one.quantity)")
                    .font(SimmerlyFont.labelMedium.weight(.semibold))
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(SimmerlyColor.surfaceContainer, in: RoundedRectangle(cornerRadius: 8))
                    .accessibilityLabel("Quantity \(one.quantity)")
            }
        case .severalRecipes(let several):
            FooterDivider()
            FooterRow(
                title: "\(several.recipeCount) recipes",
                subtitle: several.recipeNames.map(\.localized).joined(separator: ", ")
            ) {
                AvatarStack(recipes: several.recipes, recipeCount: Int(several.recipeCount))
            }
        case .addedByHand(let byHand):
            FooterDivider()
            FooterRow(title: "Added by hand", subtitle: handAddedPreview(byHand)) {
                Image(systemName: "square.and.pencil")
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    .frame(width: 48, height: 48)
                    .background(SimmerlyColor.surfaceContainer, in: RoundedRectangle(cornerRadius: 8))
                    .accessibilityHidden(true)
            }
        case .recipesAndExtras(let mixed):
            FooterDivider()
            VStack(alignment: .leading, spacing: 12) {
                FooterRow(
                    title: "\(mixed.recipeCount == 1 ? "1 recipe" : "\(mixed.recipeCount) recipes") and \(mixed.byHandCount == 1 ? "1 extra" : "\(mixed.byHandCount) extras")",
                    subtitle: mixed.recipeNames.map(\.localized).joined(separator: ", ")
                ) {
                    AvatarStack(recipes: mixed.recipes, recipeCount: Int(mixed.recipeCount))
                }
                FlowLayout(spacing: 8) {
                    SourceChip(
                        text: "\(mixed.fromRecipesCount) from recipes",
                        systemImage: "fork.knife",
                        foreground: SimmerlyColor.onTertiaryContainer,
                        background: SimmerlyColor.tertiaryContainer,
                        border: SimmerlyColor.tertiary
                    )
                    SourceChip(
                        text: "\(mixed.byHandCount) added by hand",
                        systemImage: "square.and.pencil",
                        foreground: SimmerlyColor.onSurfaceVariant,
                        background: SimmerlyColor.surface,
                        border: SimmerlyColor.surfaceContainerHighest
                    )
                }
            }
        }
    }

    private func handAddedPreview(_ byHand: ListCardFooterUiAddedByHand) -> String {
        var text = byHand.itemNames.map(\.localized).joined(separator: ", ")
        let more = Int(byHand.itemCount) - byHand.itemNames.count
        if more > 0 { text += ", +\(more)" }
        return text
    }
}

/// The thin coral bar showing how much of a list is checked.
struct ListProgressBar: View {
    let progress: Double

    var body: some View {
        GeometryReader { proxy in
            ZStack(alignment: .leading) {
                Capsule().fill(SimmerlyColor.outlineVariant)
                Capsule().fill(SimmerlyColor.primary)
                    .frame(width: proxy.size.width * min(max(progress, 0), 1))
            }
        }
        .frame(height: 4)
        .accessibilityHidden(true)
    }
}

/// A recipe photo, falling back to a tinted fork-and-knife tile while loading or when missing.
struct RecipeThumbnail: View {
    let imageUrl: String
    let size: CGFloat
    var cornerRadius: CGFloat = 8

    var body: some View {
        RemoteImage(url: imageUrl, targetSize: CGSize(width: size, height: size)) {
            ZStack {
                SimmerlyColor.primaryContainer
                Image(systemName: "fork.knife")
                    .font(.system(size: size / 2.5))
                    .foregroundStyle(SimmerlyColor.onPrimaryContainer)
            }
        }
        .frame(width: size, height: size)
        .clipShape(RoundedRectangle(cornerRadius: cornerRadius))
    }
}

private struct FooterDivider: View {
    var body: some View {
        Rectangle().fill(SimmerlyColor.outlineVariant).frame(height: 1)
    }
}

private struct FooterRow<Leading: View, Trailing: View>: View {
    let title: String
    let subtitle: String
    @ViewBuilder let leading: Leading
    @ViewBuilder let trailing: Trailing

    var body: some View {
        HStack(spacing: 12) {
            leading
            VStack(alignment: .leading, spacing: 0) {
                Text(title)
                    .font(SimmerlyFont.titleSmall)
                    .foregroundStyle(SimmerlyColor.onSurface)
                    .lineLimit(1)
                Text(subtitle)
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    .lineLimit(1)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            trailing
        }
    }
}

private extension FooterRow where Trailing == EmptyView {
    init(title: String, subtitle: String, @ViewBuilder leading: () -> Leading) {
        self.init(title: title, subtitle: subtitle, leading: leading, trailing: { EmptyView() })
    }
}

/// Overlapping recipe avatars, ending in a "+N" bubble when there are more recipes than shown.
private struct AvatarStack: View {
    let recipes: [RecipeThumbUi]
    let recipeCount: Int

    var body: some View {
        HStack(spacing: -10) {
            ForEach(Array(recipes.enumerated()), id: \.offset) { _, recipe in
                RecipeThumbnail(imageUrl: recipe.imageUrl, size: 32, cornerRadius: 16)
                    .padding(2)
                    .background(SimmerlyColor.surfaceContainerLow, in: Circle())
            }
            let overflow = recipeCount - recipes.count
            if overflow > 0 {
                Text("+\(overflow)")
                    .font(SimmerlyFont.labelMedium.weight(.semibold))
                    .foregroundStyle(SimmerlyColor.onSurface)
                    .frame(width: 32, height: 32)
                    .background(SimmerlyColor.surfaceContainerHigh, in: Circle())
                    .padding(2)
                    .background(SimmerlyColor.surfaceContainerLow, in: Circle())
            }
        }
        .accessibilityHidden(true)
    }
}

private struct SourceChip: View {
    let text: String
    let systemImage: String
    let foreground: Color
    let background: Color
    let border: Color

    var body: some View {
        Label(text, systemImage: systemImage)
            .font(SimmerlyFont.labelMedium)
            .foregroundStyle(foreground)
            .padding(.leading, 8)
            .padding(.trailing, 10)
            .frame(height: 28)
            .background(background, in: RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(border, lineWidth: 1))
    }
}

/// Placeholder card while the lists load.
struct ShoppingListCardSkeleton: View {
    var body: some View {
        RoundedRectangle(cornerRadius: 12)
            .fill(SimmerlyColor.surfaceContainer)
            .frame(height: 148)
            .redacted(reason: .placeholder)
            .accessibilityHidden(true)
    }
}
