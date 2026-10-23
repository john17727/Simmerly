import SwiftUI

/// The shopping lists overview's bottom accessory: a search field on the leading side and the New
/// list button filling the trailing end, in the same spot recipe details puts Start Cooking.
///
/// The keyboard covers the accessory, so the field here is a stand-in: tapping it asks the lists
/// screen to show the real search field just above the keyboard (`isEditingSearch`).
struct ShoppingListsAccessory: View {
    let model: TabAccessoryModel

    var body: some View {
        if let content = model.shoppingLists {
            HStack(spacing: 4) {
                AccessoryFieldButton(
                    systemImage: "magnifyingglass",
                    text: content.query,
                    placeholder: "Search lists",
                    action: { model.isEditingSearch = true }
                )
                Button(action: content.onNewList) {
                    Label("New list", systemImage: "plus")
                        .font(SimmerlyFont.labelLarge)
                        .foregroundStyle(SimmerlyColor.onPrimary)
                        .padding(.horizontal, 16)
                        .frame(maxHeight: .infinity)
                        .background(SimmerlyColor.primary, in: Capsule())
                }
                .buttonStyle(.plain)
            }
            .accessoryContentPadding()
        }
    }
}

/// A list's bottom accessory: a stand-in for the add-item field (see `ShoppingListsAccessory`)
/// and the Add button, which submits whatever was typed.
struct AddItemAccessory: View {
    let model: TabAccessoryModel

    var body: some View {
        if let content = model.addItem {
            HStack(spacing: 4) {
                AccessoryFieldButton(
                    systemImage: "plus.circle",
                    text: content.text,
                    placeholder: "Add an item, like 2 lb chicken thighs",
                    action: { model.isEditingAddItem = true }
                )
                AddButton(isAdding: content.isAdding, isEnabled: canSubmit(content), action: content.onSubmit)
            }
            .accessoryContentPadding()
        }
    }

    private func canSubmit(_ content: AddItemAccessoryContent) -> Bool {
        !content.text.trimmingCharacters(in: .whitespaces).isEmpty && !content.isAdding
    }
}

/// Looks like a text field but only opens the real one above the keyboard.
private struct AccessoryFieldButton: View {
    let systemImage: String
    let text: String
    let placeholder: String
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(systemName: systemImage)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                Text(text.isEmpty ? placeholder : text)
                    .font(SimmerlyFont.bodyLarge)
                    .foregroundStyle(text.isEmpty ? SimmerlyColor.onSurfaceVariant : SimmerlyColor.onSurface)
                    .lineLimit(1)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
            .frame(maxHeight: .infinity)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(placeholder)
        .accessibilityValue(text)
    }
}

/// The coral Add button, shared by the accessory and the editing bar above the keyboard.
struct AddButton: View {
    let isAdding: Bool
    let isEnabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Group {
                if isAdding {
                    ProgressView().tint(SimmerlyColor.onPrimary)
                } else {
                    Text("Add").font(SimmerlyFont.labelLarge)
                }
            }
            .foregroundStyle(SimmerlyColor.onPrimary)
            .padding(.horizontal, 18)
            .frame(maxHeight: .infinity)
            .background(SimmerlyColor.primary.opacity(isEnabled ? 1 : 0.5), in: Capsule())
        }
        .buttonStyle(.plain)
        .disabled(!isEnabled)
        .accessibilityLabel("Add item")
    }
}

private extension View {
    /// Fills the accessory's glass capsule, leaving room for the trailing button's own capsule.
    func accessoryContentPadding() -> some View {
        padding(.leading, 16)
            .padding(.vertical, 4)
            .padding(.trailing, 4)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }
}
