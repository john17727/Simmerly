import Shared
import SwiftUI

/// Section tab bar for the recipe detail screen, ported from the `PrimaryScrollableTabRow`
/// in recipes/presentation/details/RecipeDetailsCompact.kt. Tapping a tab scrolls its section
/// into view; scrolling the content selects the tab whose section sits under the bar.
struct RecipeSectionTabBar: View {
    /// Fixed so the bar's height never depends on measured geometry: sections offset their
    /// scroll anchors by it, and feeding a measured value back into their layout would put
    /// layout and measurement in a loop. Mirrors `TAB_ROW_HEIGHT` in RecipeDetailsCompact.kt.
    static let height: CGFloat = 48

    let tabs: [RecipeTab]
    let selection: RecipeTab
    let onSelect: (RecipeTab) -> Void

    var body: some View {
        ScrollViewReader { proxy in
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 0) {
                    ForEach(tabs, id: \.self) { tab in
                        tabButton(tab)
                            .id(tab)
                    }
                }
                .padding(.horizontal, 8)
            }
            .onChange(of: selection) { _, newValue in
                withAnimation(.easeInOut(duration: 0.25)) {
                    proxy.scrollTo(newValue, anchor: .center)
                }
            }
        }
        .frame(height: Self.height)
        .background(SimmerlyColor.background)
        .overlay(alignment: .bottom) {
            Rectangle()
                .fill(SimmerlyColor.outlineVariant)
                .frame(height: 1)
                .allowsHitTesting(false)
        }
    }

    private func tabButton(_ tab: RecipeTab) -> some View {
        Button {
            onSelect(tab)
        } label: {
            Text(tab.label)
                .font(SimmerlyFont.titleMedium)
                .foregroundStyle(SimmerlyColor.primary.opacity(tab == selection ? 1 : 0.6))
                .padding(.horizontal, 16)
                .frame(maxHeight: .infinity)
                .overlay(alignment: .bottom) {
                    Capsule()
                        .fill(SimmerlyColor.primary)
                        .frame(height: 3)
                        .opacity(tab == selection ? 1 : 0)
                }
                .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(tab == selection ? [.isButton, .isSelected] : .isButton)
    }
}

/// Reports each tagged section's top edge, measured in the scroll view's coordinate space.
struct SectionOffsetPreferenceKey: PreferenceKey {
    static let defaultValue: [RecipeTab: CGFloat] = [:]

    static func reduce(value: inout [RecipeTab: CGFloat], nextValue: () -> [RecipeTab: CGFloat]) {
        value.merge(nextValue()) { _, new in new }
    }
}

extension View {
    /// Tags a detail section so the tab bar can scroll to it and track it during scrolling.
    func recipeSection(_ tab: RecipeTab, in space: NamedCoordinateSpace) -> some View {
        self
            .id(tab)
            .background(
                GeometryReader { geometry in
                    Color.clear.preference(
                        key: SectionOffsetPreferenceKey.self,
                        value: [tab: geometry.frame(in: space).minY.rounded()]
                    )
                }
            )
    }
}
