import SwiftUI
import Shared

/// Section views ported from recipes/presentation/details/RecipeDetailsSections.kt.

struct IngredientsSection: View {
    let recipe: RecipeDetailUi
    let onAddServing: () -> Void
    let onRemoveServing: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Ingredients")
                .font(SimmerlyFont.headlineSmall)

            HStack {
                Text(recipe.formattedServings)
                    .foregroundStyle(SimmerlyColor.primary)
                Spacer()
                if recipe.isParsed {
                    HStack(spacing: 16) {
                        Button(action: onRemoveServing) {
                            Image(systemName: "minus.circle")
                        }
                        .disabled(recipe.servings <= 1)
                        Button(action: onAddServing) {
                            Image(systemName: "plus.circle")
                        }
                    }
                    .font(.system(size: 22))
                    .foregroundStyle(SimmerlyColor.primary)
                }
            }

            ForEach(recipe.ingredients, id: \.referenceId) { ingredient in
                IngredientEntry(ingredient: ingredient)
            }

            if !recipe.tools.isEmpty {
                Text("Tools")
                    .font(SimmerlyFont.headlineSmall)
                    .padding(.top, 16)
                ForEach(recipe.tools, id: \.id) { tool in
                    Text(tool.name)
                        .font(SimmerlyFont.bodyMedium)
                }
            }
        }
        .padding(.horizontal, 16)
    }
}

private struct IngredientEntry: View {
    let ingredient: IngredientUi

    var body: some View {
        VStack(alignment: .leading, spacing: 2) {
            HStack(alignment: .top, spacing: 4) {
                if let quantity = ingredient.formattedQuantity {
                    Text(quantity)
                        .fontWeight(.bold)
                        .foregroundStyle(SimmerlyColor.primary)
                }
                Text(ingredient.formattedDisplay)
            }
            .font(SimmerlyFont.bodyLarge)
            if let note = ingredient.note, !note.isEmpty {
                Text(note)
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.secondary)
            }
        }
    }
}

struct InstructionsSection: View {
    let instructions: [InstructionUi]
    let ingredients: [IngredientUi]

    private var ingredientsById: [String: IngredientUi] {
        Dictionary(uniqueKeysWithValues: ingredients.map { ($0.referenceId, $0) })
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 32) {
            Text("Instructions")
                .font(SimmerlyFont.headlineSmall)

            ForEach(instructions, id: \.id) { instruction in
                InstructionEntry(
                    instruction: instruction,
                    ingredients: instruction.ingredientIds.compactMap { ingredientsById[$0] }
                )
            }
        }
        .padding(.horizontal, 16)
    }
}

private struct InstructionEntry: View {
    let instruction: InstructionUi
    let ingredients: [IngredientUi]

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(instruction.summary)
                .font(SimmerlyFont.titleLarge)
                .foregroundStyle(SimmerlyColor.secondary)

            if !ingredients.isEmpty {
                FlowLayout(spacing: 8) {
                    ForEach(ingredients, id: \.referenceId) { ingredient in
                        IngredientChip(ingredient: ingredient)
                    }
                }
            }

            MarkdownText(markdown: instruction.text)
                .font(SimmerlyFont.bodyMedium)

            ForEach(instruction.images, id: \.self) { image in
                RemoteImage(url: image) {
                    Rectangle().fill(SimmerlyColor.surfaceContainer)
                }
                .aspectRatio(contentMode: .fit)
                .frame(maxWidth: .infinity)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
    }
}

private struct IngredientChip: View {
    let ingredient: IngredientUi

    var body: some View {
        HStack(spacing: 4) {
            if let quantity = ingredient.formattedQuantity {
                Text(quantity)
                    .fontWeight(.bold)
            }
            Text(ingredient.formattedDisplay)
        }
        .font(SimmerlyFont.bodySmall)
        .foregroundStyle(SimmerlyColor.onSecondaryContainer)
        .padding(.vertical, 4)
        .padding(.horizontal, 8)
        .background(SimmerlyColor.secondaryContainer, in: RoundedRectangle(cornerRadius: 8))
    }
}

struct NotesSection: View {
    let notes: [Note]

    var body: some View {
        VStack(alignment: .leading, spacing: 32) {
            Text("Notes")
                .font(SimmerlyFont.headlineSmall)

            ForEach(notes, id: \.id) { note in
                VStack(alignment: .leading, spacing: 6) {
                    if !note.title.isEmpty {
                        Text(note.title)
                            .font(SimmerlyFont.titleLarge)
                            .foregroundStyle(SimmerlyColor.secondary)
                    }
                    Text(note.text)
                        .font(SimmerlyFont.bodyMedium)
                }
            }
        }
        .padding(.horizontal, 16)
    }
}

struct NutritionSection: View {
    let nutrition: NutritionUi

    private var entries: [(String, String)] {
        [
            ("Calories", nutrition.calories),
            ("Carbohydrates", nutrition.carbohydrateContent),
            ("Cholesterol", nutrition.cholesterolContent),
            ("Fat", nutrition.fatContent),
            ("Fiber", nutrition.fiberContent),
            ("Protein", nutrition.proteinContent),
            ("Saturated Fat", nutrition.saturatedFatContent),
            ("Sodium", nutrition.sodiumContent),
            ("Sugar", nutrition.sugarContent),
            ("Trans Fat", nutrition.transFatContent),
            ("Unsaturated Fat", nutrition.unsaturatedFatContent),
        ].compactMap { title, value in
            guard let value, !value.isEmpty else { return nil }
            return (title, value)
        }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Nutrition")
                .font(SimmerlyFont.headlineSmall)

            ForEach(entries, id: \.0) { title, value in
                HStack {
                    Text(title)
                        .font(SimmerlyFont.bodyMedium)
                    Spacer()
                    Text(value)
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
            }
        }
        .padding(.horizontal, 16)
    }
}

struct RecipeSettingsSheet: View {
    let settings: Settings
    let onSettingChanged: (Settings) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Recipe Settings")
                .font(SimmerlyFont.headlineSmall)

            Toggle(
                "Public Recipe",
                isOn: Binding(
                    get: { settings.`public` },
                    set: {
                        onSettingChanged(settings.doCopy(
                            public: $0,
                            showNutrition: settings.showNutrition,
                            showAssets: settings.showAssets,
                            landscapeView: settings.landscapeView,
                            disableComments: settings.disableComments,
                            locked: settings.locked
                        ))
                    }
                )
            )
            Toggle(
                "Show Nutrition Values",
                isOn: Binding(
                    get: { settings.showNutrition },
                    set: {
                        onSettingChanged(settings.doCopy(
                            public: settings.`public`,
                            showNutrition: $0,
                            showAssets: settings.showAssets,
                            landscapeView: settings.landscapeView,
                            disableComments: settings.disableComments,
                            locked: settings.locked
                        ))
                    }
                )
            )
            Toggle(
                "Disable Comments",
                isOn: Binding(
                    get: { settings.disableComments },
                    set: {
                        onSettingChanged(settings.doCopy(
                            public: settings.`public`,
                            showNutrition: settings.showNutrition,
                            showAssets: settings.showAssets,
                            landscapeView: settings.landscapeView,
                            disableComments: $0,
                            locked: settings.locked
                        ))
                    }
                )
            )
            Toggle(
                "Locked",
                isOn: Binding(
                    get: { settings.locked },
                    set: {
                        onSettingChanged(settings.doCopy(
                            public: settings.`public`,
                            showNutrition: settings.showNutrition,
                            showAssets: settings.showAssets,
                            landscapeView: settings.landscapeView,
                            disableComments: settings.disableComments,
                            locked: $0
                        ))
                    }
                )
            )

            Spacer()
        }
        .padding(16)
        .tint(SimmerlyColor.primary)
    }
}
