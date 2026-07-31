import Shared
import SwiftUI

struct RecipeDetailsView: View {
    let state: RecipeDetailsState
    let onAddServing: () -> Void
    let onRemoveServing: () -> Void
    let onShowSettings: () -> Void
    let onNavigateToComments: () -> Void

    /// Section tops, keyed by tab, measured against `scrollSpace`.
    @State private var sectionOffsets: [RecipeTab: CGFloat] = [:]
    /// Bottom edge of the tab bar in `scrollSpace` — the line a section has to cross to count
    /// as the active one. Once the bar pins, this stops moving.
    @State private var tabBarBottom: CGFloat = 0
    @State private var isAtBottom = false
    @State private var contentOffsetY: CGFloat = 0
    @State private var scrollPosition = ScrollPosition()
    /// `ScrollPosition.scrollTo(y:)` takes a content coordinate, while `contentOffset` is
    /// measured from the top inset, so converting between them needs this.
    @State private var contentInsetTop: CGFloat = 0
    /// Holds the selection while a tap-driven scroll animates, so the tab doesn't
    /// flicker through the sections it passes over. Mirrors `programmaticScrollTargetIndex`.
    @State private var pendingTab: RecipeTab?

    private let scrollSpace = NamedCoordinateSpace.named("recipeDetailsScroll")

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
        .toolbar {
            ToolbarItem(placement: .primaryAction) {
                Menu {
                    Button(
                        "Recipe Settings",
                        systemImage: "gearshape",
                        action: onShowSettings
                    )
                    if !state.recipe.settings.disableComments {
                        Button(
                            "Comments",
                            systemImage: "bubble.left.and.bubble.right",
                            action: onNavigateToComments
                        )
                    }
                } label: {
                    Image(systemName: "ellipsis.circle")
                }
            }
        }
    }

    private var content: some View {
        ScrollViewReader { proxy in
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 16, pinnedViews: [.sectionHeaders]) {
                    if state.isRefreshing {
                        ProgressView()
                            .frame(maxWidth: .infinity)
                    }

                    overview
                        .recipeSection(.overview, in: scrollSpace)

                    Section {
                        // Deliberately an eager VStack: only the pinned header needs the
                        // enclosing stack to be lazy, and with a handful of cards laziness
                        // buys nothing while leaving unrealised sections unmeasured — which
                        // is exactly what `select(_:using:)` needs to scroll to them.
                        VStack(alignment: .leading, spacing: 16) {
                            IngredientsSection(
                                recipe: state.recipe,
                                onAddServing: onAddServing,
                                onRemoveServing: onRemoveServing
                            )
                            .recipeSection(.ingredients, in: scrollSpace)

                            InstructionsSection(
                                instructions: state.recipe.instructions,
                                ingredients: state.recipe.ingredients
                            )
                            .recipeSection(.instructions, in: scrollSpace)

                            if !state.recipe.notes.isEmpty {
                                NotesSection(notes: state.recipe.notes)
                                    .recipeSection(.notes, in: scrollSpace)
                            }

                            if state.recipe.settings.showNutrition {
                                NutritionSection(nutrition: state.recipe.nutrition)
                                    .recipeSection(.nutrition, in: scrollSpace)
                            }
                        }
                    } header: {
                        tabBar(proxy)
                    }
                }
                .padding(.vertical, 8)
            }
            .coordinateSpace(scrollSpace)
            .onPreferenceChange(SectionOffsetPreferenceKey.self) { offsets in
                sectionOffsets = offsets
            }
            .onScrollGeometryChange(for: Bool.self) { geometry in
                // `containerSize` is the visible content region, i.e. it already excludes the
                // content insets, so the bottom of that region sits `contentInsets.top` below
                // the raw offset. The content has bottomed out once it reaches the content end.
                geometry.contentOffset.y + geometry.contentInsets.top
                    + geometry.containerSize.height >= geometry.contentSize.height - 2
            } action: { _, atBottom in
                isAtBottom = atBottom
            }
            .onScrollGeometryChange(for: CGFloat.self) { $0.contentOffset.y } action: { _, offset in
                contentOffsetY = offset
            }
            .onScrollGeometryChange(for: CGFloat.self) { $0.contentInsets.top } action: { _, inset in
                contentInsetTop = inset
            }
            .scrollPosition($scrollPosition)
        }
        .background(SimmerlyColor.background)
    }

    @ViewBuilder
    private func tabBar(_ proxy: ScrollViewProxy) -> some View {
        if !state.mobileTabs.isEmpty {
            RecipeSectionTabBar(
                tabs: state.mobileTabs,
                selection: selectedTab,
                onSelect: { select($0, using: proxy) }
            )
            // Measured in the scroll's own space so the "scrolled past" threshold below tracks
            // the bar wherever it sits, rather than assuming where the coordinate space starts.
            // Only the threshold reads this — it must not feed back into any layout.
            .onGeometryChange(for: CGFloat.self) { $0.frame(in: scrollSpace).maxY } action: { bottom in
                tabBarBottom = bottom
            }
        }
    }

    private var overview: some View {
        VStack(alignment: .leading, spacing: 16) {
            ZStack(alignment: .bottomLeading) {
                heroImage
                TagRow(tags: state.recipe.tags)
                    .padding(8)
            }

            VStack(alignment: .leading, spacing: 8) {
                RecipeMetaRow(
                    rating: state.recipe.rating?.doubleValue,
                    totalTime: state.recipe.totalTime,
                    prepTime: state.recipe.prepTime,
                    cookTime: state.recipe.performTime
                )
                if let description = state.recipe.description_?.localized,
                    !description.isEmpty
                {
                    Text(description)
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
            }
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 8)
    }

    /// The tab whose section currently sits under the tab bar, mirroring the `derivedStateOf`
    /// block in RecipeDetailsCompact.kt: the last section scrolled past the bar wins, the last
    /// tab wins once the content bottoms out, and Overview is the fallback.
    private var selectedTab: RecipeTab {
        let tabs = state.mobileTabs
        guard let firstTab = tabs.first else { return .overview }
        if let pendingTab, tabs.contains(pendingTab) { return pendingTab }
        if isAtBottom, let lastTab = tabs.last { return lastTab }

        let active = tabs.dropFirst().last { tab in
            guard let top = sectionOffsets[tab] else { return false }
            return top <= tabBarBottom + 1
        }
        return active ?? firstTab
    }

    /// `scrollTo(_:anchor:)` aligns a section with the top of the scroll view, which leaves it
    /// tucked under the pinned bar — and it takes no offset. So scroll to a computed position
    /// instead, landing the section's top on the bar's pinned bottom edge.
    ///
    /// Two things to keep straight: the target is the bar's *pinned* bottom, which is where
    /// `scrollSpace` starts and so is just the bar's height — not wherever the bar sits right
    /// now, since before the first scroll it is still inline far down the page. And
    /// `scrollTo(y:)` wants a content coordinate, hence adding the top inset back on.
    private func select(_ tab: RecipeTab, using proxy: ScrollViewProxy) {
        pendingTab = tab
        withAnimation(.easeInOut(duration: 0.35)) {
            if tab == state.mobileTabs.first {
                scrollPosition.scrollTo(edge: .top)
            } else if let top = sectionOffsets[tab] {
                let offset = contentOffsetY + top - RecipeSectionTabBar.height
                scrollPosition.scrollTo(y: offset + contentInsetTop)
            } else {
                proxy.scrollTo(tab, anchor: .top)
            }
        }
        Task {
            try? await Task.sleep(for: .milliseconds(450))
            if pendingTab == tab { pendingTab = nil }
        }
    }

    private var heroImage: some View {
        GeometryReader { geometry in
            RemoteImage(url: state.recipe.image, targetSize: geometry.size) {
                Rectangle().fill(SimmerlyColor.surfaceContainer)
            }
        }
        .frame(height: 260)
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - Previews
// Mirrors the preview data in recipes/presentation/details/RecipeDetailsCompact.kt.

private let previewRecipe = RecipeDetailUi(
    id: "1",
    title: UiTextDynamic(text: "Spaghetti Carbonara"),
    image: "",
    description: UiTextDynamic(
        text:
            "A classic Roman pasta dish made with eggs, Pecorino Romano, guanciale, and black pepper. "
            + "Rich, creamy, and deeply satisfying without a drop of cream."
    ),
    rating: KotlinDouble(value: 4.8),
    totalTime: "30 min",
    prepTime: "10 min",
    performTime: "20 min",
    servings: 2.0,
    favorite: false,
    link: nil,
    tags: [Tag(id: "1", groupId: "", name: "Pasta")],
    ingredients: [
        IngredientUi(
            referenceId: "ingredient-1",
            quantity: nil,
            display: "200g spaghetti",
            food: nil,
            unit: nil,
            note: nil
        ),
        IngredientUi(
            referenceId: "ingredient-2",
            quantity: nil,
            display: "100g guanciale",
            food: nil,
            unit: nil,
            note: nil
        ),
        IngredientUi(
            referenceId: "ingredient-3",
            quantity: nil,
            display: "2 large eggs",
            food: nil,
            unit: nil,
            note: nil
        ),
        IngredientUi(
            referenceId: "ingredient-4",
            quantity: nil,
            display: "50g Pecorino Romano",
            food: nil,
            unit: nil,
            note: nil
        ),
    ],
    instructions: [
        InstructionUi(
            id: "1",
            title: "Cook the pasta",
            summary: "Boil spaghetti in salted water until al dente.",
            text:
                "Bring a large pot of salted water to a boil. Cook spaghetti according to package "
                + "instructions until al dente. Reserve 1 cup of pasta water before draining.",
            images: [],
            ingredientIds: ["ingredient-1"]
        ),
        InstructionUi(
            id: "2",
            title: "Prepare the sauce",
            summary: "Whisk eggs and cheese, then combine with pasta.",
            text:
                "Whisk together eggs and Pecorino Romano in a bowl. Remove pasta from heat, add "
                + "guanciale, then stir in egg mixture, adding pasta water gradually to achieve a "
                + "creamy consistency.",
            images: [],
            ingredientIds: ["ingredient-2", "ingredient-3", "ingredient-4"]
        ),
    ],
    tools: [],
    nutrition: NutritionUi(
        calories: "620 kcal",
        carbohydrateContent: "72g",
        cholesterolContent: "210mg",
        fatContent: "24g",
        fiberContent: "3g",
        proteinContent: "28g",
        saturatedFatContent: "9g",
        sodiumContent: "580mg",
        sugarContent: "2g",
        transFatContent: "0g",
        unsaturatedFatContent: "13g"
    ),
    notes: [],
    settings: Settings(
        public: true,
        showNutrition: false,
        showAssets: false,
        landscapeView: false,
        disableComments: false,
        locked: false
    )
)

private let previewState = RecipeDetailsState(
    loading: false,
    isRefreshing: false,
    recipe: previewRecipe,
    error: nil,
    mobileTabs: [.overview, .ingredients, .instructions],
    desktopTabs: [],
    mode: .readOnly,
    showSettings: false
)

#Preview {
    NavigationStack {
        RecipeDetailsView(
            state: previewState,
            onAddServing: {},
            onRemoveServing: {},
            onShowSettings: {},
            onNavigateToComments: {}
        )
    }
}

#Preview("Loading") {
    NavigationStack {
        RecipeDetailsView(
            state: RecipeDetailsState(
                loading: true,
                isRefreshing: false,
                recipe: RecipeDetailUi.companion.emptyRecipe,
                error: nil,
                mobileTabs: [],
                desktopTabs: [],
                mode: .readOnly,
                showSettings: false
            ),
            onAddServing: {},
            onRemoveServing: {},
            onShowSettings: {},
            onNavigateToComments: {}
        )
    }
}
