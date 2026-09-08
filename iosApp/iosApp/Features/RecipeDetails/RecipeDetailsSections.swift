import SwiftUI
import Shared

/// Section views ported from recipes/presentation/details/RecipeDetailsSections.kt.

struct IngredientsSection: View {
    let recipe: RecipeDetailUi
    let onAddServing: () -> Void
    let onRemoveServing: () -> Void

    var body: some View {
        SectionCard {
            VStack(alignment: .leading, spacing: 0) {
                Text("Ingredients")
                    .font(SimmerlyFont.headlineSmall)

                HStack {
                    Text(recipe.formattedServings)
                        .font(SimmerlyFont.labelLarge)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    Spacer()
                    if recipe.isParsed {
                        HStack(spacing: 4) {
                            ServingStepperButton(
                                systemImage: "minus",
                                label: "Fewer servings",
                                action: onRemoveServing
                            )
                            .disabled(recipe.servings <= 1)
                            ServingStepperButton(
                                systemImage: "plus",
                                label: "More servings",
                                action: onAddServing
                            )
                        }
                    }
                }
                .padding(.top, 12)

                VStack(alignment: .leading, spacing: 0) {
                    ForEach(recipe.ingredients, id: \.referenceId) { ingredient in
                        IngredientEntry(ingredient: ingredient)
                    }
                }
                .padding(.top, 16)

                if !recipe.tools.isEmpty {
                    Text("Tools")
                        .font(SimmerlyFont.headlineSmall)
                        .padding(.top, 24)
                        .padding(.bottom, 8)
                    ForEach(recipe.tools, id: \.id) { tool in
                        Text(tool.name)
                            .font(SimmerlyFont.bodyMedium)
                    }
                }

                Button {
                    // TODO: add to shopping list
                } label: {
                    HStack(spacing: 8) {
                        Image(systemName: "list.bullet.rectangle")
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        Text("Add to shopping list")
                            .font(SimmerlyFont.titleSmall)
                            .foregroundStyle(SimmerlyColor.onSurface)
                    }
                    .frame(maxWidth: .infinity, minHeight: 40)
                    .overlay(
                        Capsule().strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1)
                    )
                }
                .buttonStyle(.plain)
                .padding(.top, 16)
            }
        }
    }
}

private struct ServingStepperButton: View {
    let systemImage: String
    let label: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: systemImage)
                .font(.system(size: 15, weight: .medium))
                .frame(width: 32, height: 32)
                .overlay(Circle().strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
        .accessibilityLabel(label)
    }
}

/// Two columns: the quantity in a fixed-width gutter so every ingredient name starts on the same
/// vertical line, then the name with its note underneath. Matches IngredientEntry in
/// recipes/presentation/details/RecipeDetailsSections.kt.
private struct IngredientEntry: View {
    let ingredient: IngredientUi

    /// Wider than the design's 78px: that artboard used abbreviated units ("1 tbsp"), but Mealie
    /// spells them out when a unit has useAbbreviation off, and "2 tablespoons" has to fit.
    private static let quantityWidth: CGFloat = 104

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Divider().overlay(SimmerlyColor.surfaceContainer)
            HStack(alignment: .top, spacing: 10) {
                Text(ingredient.formattedQuantity ?? "")
                    .font(SimmerlyFont.titleSmall)
                    .foregroundStyle(SimmerlyColor.secondary)
                    .frame(width: Self.quantityWidth, alignment: .leading)
                VStack(alignment: .leading, spacing: 0) {
                    Text(ingredient.formattedDisplay)
                        .font(SimmerlyFont.bodyMedium)
                    if let note = ingredient.note, !note.isEmpty {
                        Text(note)
                            .font(SimmerlyFont.bodySmall)
                            .foregroundStyle(SimmerlyPalette.neutral500)
                    }
                }
                Spacer(minLength: 0)
            }
            .padding(.vertical, 9)
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
        SectionCard {
            VStack(alignment: .leading, spacing: 0) {
                Text("Instructions")
                    .font(SimmerlyFont.headlineSmall)

                ForEach(Array(instructions.enumerated()), id: \.element.id) { index, instruction in
                    InstructionEntry(
                        stepNumber: index + 1,
                        instruction: instruction,
                        ingredients: instruction.ingredientIds.compactMap { ingredientsById[$0] }
                    )
                    // Only the first step clears the heading; the rest butt up against the
                    // divider that closes the step above them.
                    .padding(.top, index == 0 ? 12 : 0)
                }
            }
        }
    }
}

private struct InstructionEntry: View {
    let stepNumber: Int
    let instruction: InstructionUi
    let ingredients: [IngredientUi]

    /// Step photos are cropped to a consistent band rather than each taking whatever height its
    /// aspect ratio implies, matching the design.
    private static let imageHeight: CGFloat = 260

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Divider().overlay(SimmerlyColor.outlineVariant)
            HStack(alignment: .top, spacing: 20) {
                Text("\(stepNumber)")
                    .font(SimmerlyFont.titleSmall)
                    .fontWeight(.bold)
                    .foregroundStyle(SimmerlyColor.secondary)
                    .frame(width: 36, height: 36)
                    .background(SimmerlyColor.primaryContainer, in: Circle())

                VStack(alignment: .leading, spacing: 16) {
                    // Skip the generated "Step N" heading — the badge to the left already says it.
                    if instruction.hasOwnSummary && !instruction.summary.isEmpty {
                        Text(instruction.summary)
                            .font(SimmerlyFont.titleLarge)
                            .foregroundStyle(SimmerlyColor.secondary)
                    }

                    if !ingredients.isEmpty {
                        FlowLayout(spacing: 8) {
                            ForEach(ingredients, id: \.referenceId) { ingredient in
                                IngredientChip(ingredient: ingredient)
                            }
                        }
                    }

                    MarkdownText(markdown: instruction.text)
                        .font(SimmerlyFont.bodyLarge)

                    ForEach(instruction.images, id: \.self) { image in
                        // The image goes in an overlay over a fixed-size spacer rather than
                        // being sized directly: `.aspectRatio(.fill)` reports whatever width it
                        // needs to cover the height, and `.frame(maxWidth: .infinity)` does not
                        // clamp that, so sizing it directly widens the whole card off-screen.
                        Color.clear
                            .frame(maxWidth: .infinity)
                            .frame(height: Self.imageHeight)
                            .overlay {
                                RemoteImage(url: image) {
                                    Rectangle().fill(SimmerlyColor.surfaceContainer)
                                }
                                .aspectRatio(contentMode: .fill)
                            }
                            .clipShape(RoundedRectangle(cornerRadius: 12))
                    }
                }
                Spacer(minLength: 0)
            }
            .padding(.vertical, 20)
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
        .foregroundStyle(SimmerlyColor.onTertiaryContainer)
        .padding(.vertical, 4)
        .padding(.horizontal, 8)
        .background(SimmerlyColor.tertiaryContainer, in: RoundedRectangle(cornerRadius: 8))
    }
}

struct NotesSection: View {
    let notes: [Note]

    var body: some View {
        SectionCard {
            VStack(alignment: .leading, spacing: 24) {
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
        }
    }
}

struct NutritionSection: View {
    let nutrition: NutritionUi

    var body: some View {
        SectionCard {
            VStack(alignment: .leading, spacing: 0) {
                Text("Nutrition")
                    .font(SimmerlyFont.titleLarge)
                    .padding(.bottom, 12)

                // The label list and its order live in NutritionUi.kt so both platforms render the
                // same facts; this used to be a second copy that had to be kept in step by hand.
                ForEach(nutrition.entries, id: \.label) { entry in
                    VStack(alignment: .leading, spacing: 0) {
                        Divider().overlay(SimmerlyColor.surfaceContainer)
                        HStack {
                            Text(entry.label)
                                .font(SimmerlyFont.bodySmall)
                            Spacer()
                            Text(entry.value)
                                .font(SimmerlyFont.bodySmall)
                                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        }
                        .padding(.vertical, 7)
                    }
                }
            }
        }
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
