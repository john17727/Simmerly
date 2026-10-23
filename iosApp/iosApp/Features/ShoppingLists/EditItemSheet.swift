import SwiftUI
import Shared

/// Edits one item's quantity, unit, food and note. Mirrors
/// shoppinglists/presentation/detail/EditItemSheet.kt: choosing a food files the item under that
/// food's aisle; clearing the food field leaves a note-only item.
struct EditItemSheet: View {
    let editor: ItemEditorUi
    let units: [UnitOptionUi]
    let foodSuggestions: [FoodOptionUi]
    let onEvent: (ShoppingListDetailIntent) -> Void

    var body: some View {
        NavigationStack {
            Form {
                Section {
                    TextField(
                        "Quantity",
                        text: Binding(
                            get: { editor.quantity },
                            set: { onEvent(ShoppingListDetailIntentOnEditorQuantityChanged(quantity: $0)) }
                        )
                    )
                    .keyboardType(.decimalPad)
                    Picker(
                        "Unit",
                        selection: Binding(
                            get: { editor.unitId },
                            set: { onEvent(ShoppingListDetailIntentOnEditorUnitSelected(unitId: $0)) }
                        )
                    ) {
                        Text("No unit").tag(String?.none)
                        ForEach(units, id: \.id) { unit in
                            Text(unit.name.localized).tag(Optional(unit.id))
                        }
                    }
                }
                Section {
                    TextField(
                        "Search foods",
                        text: Binding(
                            get: { editor.foodQuery },
                            set: { onEvent(ShoppingListDetailIntentOnEditorFoodQueryChanged(query: $0)) }
                        )
                    )
                    .textInputAutocapitalization(.never)
                    ForEach(foodSuggestions, id: \.id) { food in
                        Button {
                            onEvent(ShoppingListDetailIntentOnEditorFoodSelected(food: food))
                        } label: {
                            HStack {
                                Text(food.name.localized).foregroundStyle(SimmerlyColor.onSurface)
                                Spacer()
                                Text(food.aisle?.localized ?? "No aisle")
                                    .font(SimmerlyFont.bodySmall)
                                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                            }
                        }
                    }
                } header: {
                    Text("Food")
                } footer: {
                    Text("Foods put the item in their aisle. Leave empty for a plain note.")
                }
                Section(editor.food == nil ? "Item" : "Note") {
                    TextField(
                        editor.food == nil ? "Item" : "Note",
                        text: Binding(
                            get: { editor.note },
                            set: { onEvent(ShoppingListDetailIntentOnEditorNoteChanged(note: $0)) }
                        )
                    )
                    .textInputAutocapitalization(.sentences)
                }
                Section {
                    Button("Delete item", role: .destructive) {
                        onEvent(ShoppingListDetailIntentOnEditorDeleteClicked.shared)
                    }
                }
            }
            .navigationTitle("Edit item")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { onEvent(ShoppingListDetailIntentOnEditorDismissed.shared) }
                }
                ToolbarItem(placement: .confirmationAction) {
                    if editor.isSaving {
                        ProgressView()
                    } else {
                        Button("Save") { onEvent(ShoppingListDetailIntentOnEditorSaveClicked.shared) }
                            .disabled(editor.food == nil && editor.note.trimmingCharacters(in: .whitespaces).isEmpty)
                    }
                }
            }
        }
        .presentationDetents([.medium, .large])
    }
}

// MARK: - Previews

#Preview("Food item") {
    EditItemSheet(
        editor: ItemEditorUi(
            itemId: "1", quantity: "2", unitId: "cup", foodQuery: "milk",
            food: FoodOptionUi(id: "milk", name: UiTextDynamic(text: "milk"), aisle: UiTextDynamic(text: "Dairy")),
            note: "oat", isSaving: false
        ),
        units: [UnitOptionUi(id: "cup", name: UiTextDynamic(text: "cup"))],
        foodSuggestions: [],
        onEvent: { _ in }
    )
}

#Preview("Note only") {
    EditItemSheet(
        editor: ItemEditorUi(
            itemId: "1", quantity: "1", unitId: nil, foodQuery: "", food: nil,
            note: "Paper napkins", isSaving: false
        ),
        units: [],
        foodSuggestions: [],
        onEvent: { _ in }
    )
}
