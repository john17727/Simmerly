import SwiftUI

/// Outlined container for the recipe detail sections.
/// Mirrors the `OutlinedCard` wrappers in recipes/presentation/details/RecipeDetailsCompact.kt:
/// surface fill, 1dp `outlineVariant` border, medium corners, 20dp inner padding (SECTION_PADDING
/// in RecipeDetailsSections.kt) and 16dp horizontal inset from the screen edge.
struct SectionCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        content
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(20)
            .background(SimmerlyColor.surface, in: RoundedRectangle(cornerRadius: 12))
            .overlay(
                RoundedRectangle(cornerRadius: 12).strokeBorder(
                    SimmerlyColor.outlineVariant,
                    lineWidth: 1
                )
            )
            .padding(.horizontal, 16)
    }
}
