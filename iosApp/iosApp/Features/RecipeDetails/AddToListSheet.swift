import SwiftUI
import Shared

/// The "Add to shopping list" flow: choose a list (or name a new one), then pick which
/// ingredients to add and for how many servings. Mirrors
/// recipes/presentation/details/AddToListSheet.kt; the step order and back navigation live in
/// `RecipeDetailsViewModel`, so this only renders `AddToListState` and sends intents.
struct AddToListSheet: View {
    let sheet: AddToListState
    let recipeTitle: String
    let ingredients: [IngredientUi]
    let onEvent: (RecipeDetailsIntent) -> Void

    /// Follows the step: the ingredient list wants the full height (Android fills 90% there),
    /// while choosing a list or naming one fits the medium detent.
    @State private var detent: PresentationDetent = .medium

    var body: some View {
        NavigationStack {
            Group {
                switch sheet.step {
                case .chooseList: chooseList
                case .newListName: newListName
                case .pickIngredients: pickIngredients
                }
            }
            .navigationTitle(title)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    if sheet.step == .chooseList {
                        Button("Cancel") { onEvent(RecipeDetailsIntentDismissAddToList.shared) }
                    } else {
                        Button("Back", systemImage: "chevron.backward") {
                            onEvent(RecipeDetailsIntentAddToListBack.shared)
                        }
                    }
                }
                if sheet.step == .newListName {
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Next") { onEvent(RecipeDetailsIntentAddToListNameConfirmed.shared) }
                            .disabled(sheet.newListName.trimmingCharacters(in: .whitespaces).isEmpty)
                    }
                }
            }
        }
        .disabled(sheet.isSubmitting)
        .interactiveDismissDisabled(sheet.isSubmitting)
        .presentationDetents([.medium, .large], selection: $detent)
        .onChange(of: sheet.step) { _, step in
            detent = step == .pickIngredients ? .large : .medium
        }
        .tint(SimmerlyColor.primary)
    }

    private var title: LocalizedStringKey {
        switch sheet.step {
        case .chooseList: "Add to shopping list"
        case .newListName: "New list"
        case .pickIngredients: "Add ingredients"
        }
    }

    // MARK: Choose list

    private var chooseList: some View {
        Form {
            Section {
                Picker(
                    "Lists",
                    selection: Binding(
                        get: { sheet.filter },
                        set: { onEvent(RecipeDetailsIntentAddToListFilterChanged(filter: $0)) }
                    )
                ) {
                    Text("Mine").tag(ListFilter.mine)
                    Text("Household").tag(ListFilter.household)
                }
                .pickerStyle(.segmented)
            } footer: {
                Text("For \(recipeTitle)")
            }

            Section {
                Button {
                    onEvent(RecipeDetailsIntentAddToListNewListClicked.shared)
                } label: {
                    Label("New list", systemImage: "plus")
                }

                if let lists = sheet.lists {
                    ForEach(lists, id: \.id) { list in
                        Button {
                            onEvent(RecipeDetailsIntentAddToListListChosen(listId: list.id))
                        } label: {
                            listRow(list)
                        }
                    }
                } else {
                    ProgressView().frame(maxWidth: .infinity)
                }
            }
        }
    }

    private func listRow(_ list: AddToListRowUi) -> some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(list.name)
                    .foregroundStyle(SimmerlyColor.onSurface)
                Text(list.alreadyHasRecipe ? "Already has this recipe" : itemCountText(list.itemCount))
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }
            Spacer()
            Image(systemName: "chevron.right")
                .font(.footnote)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
        }
    }

    private func itemCountText(_ count: Int32) -> String {
        count == 1 ? "1 item" : "\(count) items"
    }

    // MARK: New list

    private var newListName: some View {
        Form {
            Section {
                HStack {
                    TextField(
                        "List name",
                        text: Binding(
                            get: { sheet.newListName },
                            set: { onEvent(RecipeDetailsIntentAddToListNameChanged(name: $0)) }
                        )
                    )
                    .textInputAutocapitalization(.sentences)
                    .submitLabel(.next)
                    .onSubmit { onEvent(RecipeDetailsIntentAddToListNameConfirmed.shared) }
                    if !sheet.newListName.isEmpty {
                        Button {
                            onEvent(RecipeDetailsIntentAddToListNameChanged(name: ""))
                        } label: {
                            Image(systemName: "xmark.circle.fill")
                                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Clear name")
                    }
                }
            } footer: {
                Text("For \(recipeTitle)")
            }

            Section("Or use") {
                Button {
                    onEvent(RecipeDetailsIntentAddToListNameChanged(name: recipeTitle))
                } label: {
                    HStack {
                        Label(recipeTitle, systemImage: "fork.knife")
                            .foregroundStyle(SimmerlyColor.onSurface)
                        Spacer()
                        if sheet.newListName == recipeTitle {
                            Image(systemName: "checkmark")
                                .foregroundStyle(SimmerlyColor.primary)
                        }
                    }
                }
                .accessibilityAddTraits(sheet.newListName == recipeTitle ? .isSelected : [])
            }
        }
    }

    // MARK: Pick ingredients

    private var pickIngredients: some View {
        Form {
            Section {
                HStack {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(servingsText)
                            .font(SimmerlyFont.titleSmall)
                        Text("Amounts scale to match")
                            .font(SimmerlyFont.bodySmall)
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }
                    Spacer()
                    HStack(spacing: 4) {
                        AddToListStepperButton(systemImage: "minus", label: "Fewer servings") {
                            onEvent(RecipeDetailsIntentAddToListRemoveServing.shared)
                        }
                        .disabled(sheet.servings <= 1)
                        AddToListStepperButton(systemImage: "plus", label: "More servings") {
                            onEvent(RecipeDetailsIntentAddToListAddServing.shared)
                        }
                    }
                }
            } header: {
                Text("From \(recipeTitle)")
            }

            Section {
                ForEach(Array(ingredients.enumerated()), id: \.element.referenceId) { index, ingredient in
                    Button {
                        onEvent(RecipeDetailsIntentAddToListToggleIngredient(index: Int32(index)))
                    } label: {
                        HStack(spacing: 12) {
                            SimmerlyCheckbox(checked: sheet.selected.contains(KotlinInt(int: Int32(index))))
                            VStack(alignment: .leading, spacing: 0) {
                                Text(ingredientText(ingredient))
                                    .font(SimmerlyFont.bodyMedium)
                                    .foregroundStyle(SimmerlyColor.onSurface)
                                if let note = ingredient.note, !note.isEmpty {
                                    Text(note)
                                        .font(SimmerlyFont.bodySmall)
                                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                                }
                            }
                            Spacer(minLength: 0)
                        }
                    }
                    .accessibilityAddTraits(
                        sheet.selected.contains(KotlinInt(int: Int32(index))) ? .isSelected : []
                    )
                }
            } header: {
                HStack {
                    Text("Ingredients")
                    Spacer()
                    Button(allSelected ? "Clear all" : "Select all") {
                        onEvent(RecipeDetailsIntentAddToListToggleAll.shared)
                    }
                    .textCase(nil)
                }
            }
        }
        .safeAreaInset(edge: .bottom, spacing: 0) {
            Button {
                onEvent(RecipeDetailsIntentAddToListConfirm.shared)
            } label: {
                Group {
                    if sheet.isSubmitting {
                        ProgressView()
                    } else {
                        Text(confirmLabel)
                    }
                }
                .frame(maxWidth: .infinity, minHeight: 28)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
            .disabled(sheet.selected.isEmpty || sheet.isSubmitting)
            .padding(16)
            .background(.bar)
        }
    }

    private var allSelected: Bool {
        !ingredients.isEmpty && sheet.selected.count == ingredients.count
    }

    private var servingsText: String {
        let count = sheet.servings.formatted(.number.precision(.fractionLength(0...1)))
        return sheet.servings == 1 ? "\(count) serving" : "\(count) servings"
    }

    private func ingredientText(_ ingredient: IngredientUi) -> String {
        [ingredient.formattedQuantity, ingredient.formattedDisplay]
            .compactMap { $0 }
            .filter { !$0.isEmpty }
            .joined(separator: " ")
    }

    private var confirmLabel: LocalizedStringKey {
        let count = sheet.selected.count
        let isNew = sheet.target is AddToListTargetNew
        switch (count, isNew) {
        case (0, _): return "Select ingredients to add"
        case (1, true): return "Create list with 1 item"
        case (_, true): return "Create list with \(count) items"
        case (1, false): return "Add 1 item"
        default: return "Add \(count) items"
        }
    }
}

private struct AddToListStepperButton: View {
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
