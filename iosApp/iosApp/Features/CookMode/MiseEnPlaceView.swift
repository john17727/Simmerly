import SwiftUI
import Shared

/// Mirrors recipes/presentation/cookmode/MiseEnPlaceView.kt (design frame 02): every ingredient
/// gets checked off, and tools are called out, before a cook moves into the timed steps. "Add
/// missing" is drawn per the design but intentionally inert — there is no shopping-list feature
/// in the app for it to add to.
struct MiseEnPlaceView: View {
    let state: CookModeState
    let onEvent: (CookModeIntent) -> Void
    let onExit: () -> Void

    private var recipe: RecipeDetailUi { state.recipe }
    private var readyCount: Int { Int(state.readyIngredientCount) }
    private var totalCount: Int { recipe.ingredients.count }
    private var progress: Double { totalCount == 0 ? 0 : Double(readyCount) / Double(totalCount) }
    private var allIngredientsChecked: Bool { totalCount == 0 || readyCount == totalCount }
    private var canStartCooking: Bool { !recipe.instructions.isEmpty && allIngredientsChecked }

    var body: some View {
        VStack(spacing: 0) {
            header
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(recipe.ingredients, id: \.referenceId) { ingredient in
                        CheckableIngredientRow(
                            ingredient: ingredient,
                            checked: state.checkedIngredientIds.contains(ingredient.referenceId),
                            onToggle: { onEvent(CookModeIntentToggleIngredient(referenceId: ingredient.referenceId)) }
                        )
                    }
                    if !recipe.tools.isEmpty {
                        ToolsSection(tools: recipe.tools)
                    }
                }
            }
            bottomBar
        }
        .background(SimmerlyColor.background)
    }

    private var header: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onExit) {
                    Image(systemName: "xmark")
                        .foregroundStyle(SimmerlyColor.onSurface)
                }
                Text("Mise en place")
                    .font(SimmerlyFont.headlineSmall)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Button("Skip") { onEvent(CookModeIntentSkipMiseEnPlace.shared) }
            }
            .padding(.horizontal, 24)
            .frame(height: 64)

            HStack(spacing: 12) {
                Text(recipe.title.localized)
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)

                if recipe.isParsed {
                    HStack(spacing: 2) {
                        ServingStepperButton(
                            systemImage: "minus",
                            enabled: recipe.servings > 1,
                            action: { onEvent(CookModeIntentRemoveServing.shared) }
                        )
                        Text(recipe.formattedServings)
                            .font(SimmerlyFont.labelMedium)
                            .foregroundStyle(SimmerlyColor.onSurface)
                            .frame(minWidth: 76)
                            .multilineTextAlignment(.center)
                        ServingStepperButton(
                            systemImage: "plus",
                            enabled: true,
                            action: { onEvent(CookModeIntentAddServing.shared) }
                        )
                    }
                } else {
                    Text(recipe.formattedServings)
                        .font(SimmerlyFont.bodySmall)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 4)

            HStack {
                Text("\(readyCount) OF \(totalCount) READY")
                    .font(SimmerlyFont.labelSmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                Spacer()
                HStack(spacing: 6) {
                    Image(systemName: "basket")
                        .font(.system(size: 14))
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    Text("Add missing")
                        .font(SimmerlyFont.labelMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(SimmerlyColor.surfaceContainer, in: Capsule())
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 8)

            ProgressView(value: progress)
                .tint(SimmerlyColor.primary)
                .padding(.horizontal, 24)
                .padding(.bottom, 8)
        }
    }

    private var bottomBar: some View {
        VStack(spacing: 8) {
            HStack(spacing: 8) {
                Image(systemName: "eye")
                    .font(.system(size: 14))
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                Text("Screen stays awake while you cook.")
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }
            Button {
                onEvent(CookModeIntentBeginSteps.shared)
            } label: {
                Text("Start cooking")
                    .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(!canStartCooking)
            .controlSize(.large)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
}

private struct CheckableIngredientRow: View {
    let ingredient: IngredientUi
    let checked: Bool
    let onToggle: () -> Void

    var body: some View {
        Button(action: onToggle) {
            HStack(alignment: .top, spacing: 16) {
                ZStack {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(checked ? SimmerlyColor.primary : Color.clear)
                    RoundedRectangle(cornerRadius: 4)
                        .strokeBorder(checked ? Color.clear : SimmerlyColor.outline, lineWidth: 2)
                    if checked {
                        Image(systemName: "checkmark")
                            .font(.system(size: 12, weight: .bold))
                            .foregroundStyle(SimmerlyColor.onPrimary)
                    }
                }
                .frame(width: 22, height: 22)

                VStack(alignment: .leading, spacing: 2) {
                    HStack(alignment: .top, spacing: 4) {
                        if let quantity = ingredient.formattedQuantity {
                            Text(quantity)
                                .fontWeight(.bold)
                                .strikethrough(checked)
                                .foregroundStyle(checked ? SimmerlyColor.onSurfaceVariant : SimmerlyColor.primary)
                        }
                        Text(ingredient.formattedDisplay)
                            .strikethrough(checked)
                            .foregroundStyle(checked ? SimmerlyColor.onSurfaceVariant : SimmerlyColor.onSurface)
                    }
                    .font(SimmerlyFont.bodyLarge)

                    if let note = ingredient.note, !note.isEmpty {
                        Text(note)
                            .font(SimmerlyFont.bodySmall)
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }
                }
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 10)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}

private struct ToolsSection: View {
    let tools: [Tool]

    var body: some View {
        VStack(alignment: .leading, spacing: 10) {
            Text("TOOLS")
                .font(SimmerlyFont.labelSmall)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            FlowLayout(spacing: 8) {
                ForEach(tools, id: \.id) { tool in
                    TagChip(name: tool.name)
                }
            }
        }
        .padding(.horizontal, 24)
        .padding(.vertical, 16)
    }
}

private struct ServingStepperButton: View {
    let systemImage: String
    let enabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: 14))
                .foregroundStyle(SimmerlyColor.onSurfaceVariant.opacity(enabled ? 1 : 0.4))
                .frame(width: 32, height: 32)
                .background(SimmerlyColor.surfaceContainer, in: Circle())
        }
        .disabled(!enabled)
    }
}

// MARK: - Previews
// Mirrors the preview data in recipes/presentation/cookmode/CookStepView.kt (previewCookRecipe)
// and MiseEnPlaceView.kt (previewMiseEnPlaceState) — five ingredients, two checked, three tools.

private let previewCookRecipe = RecipeDetailUi(
    id: "preview-recipe",
    title: UiTextDynamic(text: "Spaghetti Carbonara"),
    image: "",
    description: UiTextDynamic(
        text: "A classic Roman pasta dish made with eggs, Pecorino Romano, guanciale, and black pepper."
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
            quantity: KotlinDouble(value: 200.0),
            display: "200 g Spaghetti",
            food: FoodUi(name: "spaghetti", pluralName: "spaghetti"),
            unit: UnitUi(
                name: "gram",
                pluralName: "grams",
                fraction: false,
                abbreviation: "g",
                pluralAbbreviation: "g",
                useAbbreviation: true
            ),
            note: nil
        ),
        IngredientUi(
            referenceId: "ingredient-2",
            quantity: KotlinDouble(value: 100.0),
            display: "100 g Guanciale",
            food: nil,
            unit: nil,
            note: "pancetta works too"
        ),
        IngredientUi(
            referenceId: "ingredient-3",
            quantity: KotlinDouble(value: 2.0),
            display: "2 Large eggs",
            food: nil,
            unit: nil,
            note: nil
        ),
        IngredientUi(
            referenceId: "ingredient-4",
            quantity: KotlinDouble(value: 50.0),
            display: "50 g Pecorino Romano",
            food: nil,
            unit: nil,
            note: nil
        ),
    ],
    instructions: [
        InstructionUi(
            id: "step-1",
            title: nil,
            summary: "Step 1",
            text: "Bring a large pot of salted water to a boil. Cook the spaghetti for 15–17 minutes, until al dente.",
            images: [],
            ingredientIds: ["ingredient-1"]
        ),
        InstructionUi(
            id: "step-2",
            title: nil,
            summary: "Step 2",
            text: "Cut the guanciale into short batons. Cook for 8 minutes, until the fat has run clear and the edges are crisp.",
            images: [],
            ingredientIds: ["ingredient-2"]
        ),
        InstructionUi(
            id: "step-3",
            title: nil,
            summary: "Step 3",
            text: "Whisk the eggs and grated pecorino into a thick paste. Grind in the pepper.",
            images: [],
            ingredientIds: ["ingredient-3", "ingredient-4"]
        ),
    ],
    tools: [
        Tool(id: "tool-1", groupId: "", name: "Large pot"),
        Tool(id: "tool-2", groupId: "", name: "Heavy skillet"),
        Tool(id: "tool-3", groupId: "", name: "Mixing bowl"),
    ],
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

private func previewMiseEnPlaceState(checkedAll: Bool = false) -> CookModeState {
    let checked: Set<String> = checkedAll
        ? Set(previewCookRecipe.ingredients.map(\.referenceId))
        : ["ingredient-1", "ingredient-2"]
    return CookModeState(
        loading: false,
        recipe: previewCookRecipe,
        error: nil,
        phase: .miseEnPlace,
        stepIndex: 0,
        checkedIngredientIds: checked,
        timers: [],
        notifiedFinishedTimerIds: [],
        nowMillis: 0,
        cookingStartedAtMillis: nil,
        cookingFinishedAtMillis: nil,
        showTimerList: false,
        newTimerDraft: nil,
        selectedRangeOption: nil,
        rating: 0,
        noteDraft: ""
    )
}

#Preview {
    MiseEnPlaceView(state: previewMiseEnPlaceState(), onEvent: { _ in }, onExit: {})
}

#Preview("All Checked") {
    MiseEnPlaceView(state: previewMiseEnPlaceState(checkedAll: true), onEvent: { _ in }, onExit: {})
}
