import Shared
import SwiftUI

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
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                if state.isRefreshing {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                }

                heroImage

                VStack(alignment: .leading, spacing: 8) {
                    RecipeMetaRow(
                        rating: state.recipe.rating?.doubleValue,
                        totalTime: state.recipe.totalTime,
                        prepTime: state.recipe.prepTime,
                        cookTime: state.recipe.performTime
                    )
                    TagRow(tags: state.recipe.tags)
                    if let description = state.recipe.description_?.localized,
                        !description.isEmpty
                    {
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
    tags: [],
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

#Preview("Light") {
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

#Preview("Dark") {
    NavigationStack {
        RecipeDetailsView(
            state: previewState,
            onAddServing: {},
            onRemoveServing: {},
            onShowSettings: {},
            onNavigateToComments: {}
        )
    }
    .preferredColorScheme(.dark)
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
