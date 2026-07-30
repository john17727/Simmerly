import SwiftUI
import Shared

/// Ports of recipes/presentation/shared/RecipeSharedUI.kt.

struct TagChip: View {
    let name: String

    var body: some View {
        Text(name)
            .font(SimmerlyFont.bodySmall)
            .foregroundStyle(SimmerlyColor.onTertiaryContainer)
            .padding(.vertical, 4)
            .padding(.horizontal, 8)
            .background(SimmerlyColor.tertiaryContainer, in: RoundedRectangle(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8).strokeBorder(SimmerlyColor.tertiary, lineWidth: 1)
            )
    }
}

struct TagRow: View {
    let tags: [Tag]

    var body: some View {
        if !tags.isEmpty {
            FlowLayout(spacing: 8) {
                ForEach(tags, id: \.id) { tag in
                    TagChip(name: tag.name)
                }
            }
        }
    }
}

struct RecipeMetaRow: View {
    let rating: Double?
    let totalTime: String?
    let prepTime: String?
    let cookTime: String?

    var body: some View {
        HStack(spacing: 8) {
            if let rating {
                MetaItem(systemImage: "star.fill", text: "\(rating)", tint: SimmerlyColor.onSurfaceVariant)
            }
            if let totalTime {
                MetaItem(systemImage: "timer", text: totalTime, tint: SimmerlyColor.primary)
            }
            if let prepTime {
                MetaItem(systemImage: "fork.knife", text: prepTime, tint: SimmerlyColor.primary)
            }
            if let cookTime {
                MetaItem(systemImage: "flame", text: cookTime, tint: SimmerlyColor.primary)
            }
        }
    }
}

private struct MetaItem: View {
    let systemImage: String
    let text: String
    let tint: Color

    var body: some View {
        HStack(spacing: 4) {
            Image(systemName: systemImage)
                .font(.system(size: 12))
                .foregroundStyle(tint)
            Text(text)
                .font(SimmerlyFont.bodySmall)
        }
    }
}

/// Minimal flow layout for tag chips — SwiftUI has no built-in wrap-on-overflow HStack.
struct FlowLayout: Layout {
    var spacing: CGFloat = 8

    func sizeThatFits(proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) -> CGSize {
        let width = proposal.width ?? .infinity
        var rowWidth: CGFloat = 0
        var totalHeight: CGFloat = 0
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if rowWidth + size.width > width, rowWidth > 0 {
                totalHeight += rowHeight + spacing
                rowWidth = 0
                rowHeight = 0
            }
            rowWidth += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
        totalHeight += rowHeight
        return CGSize(width: width == .infinity ? rowWidth : width, height: totalHeight)
    }

    func placeSubviews(in bounds: CGRect, proposal: ProposedViewSize, subviews: Subviews, cache: inout ()) {
        var x = bounds.minX
        var y = bounds.minY
        var rowHeight: CGFloat = 0

        for subview in subviews {
            let size = subview.sizeThatFits(.unspecified)
            if x + size.width > bounds.maxX, x > bounds.minX {
                x = bounds.minX
                y += rowHeight + spacing
                rowHeight = 0
            }
            subview.place(at: CGPoint(x: x, y: y), proposal: .unspecified)
            x += size.width + spacing
            rowHeight = max(rowHeight, size.height)
        }
    }
}
