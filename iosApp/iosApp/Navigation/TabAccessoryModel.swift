import SwiftUI

/// Carries a tab's bottom-accessory request up to the `TabView` that renders it.
///
/// `tabViewBottomAccessory` only takes effect when applied to the `TabView` itself — declaring it
/// deeper inside a tab's `NavigationStack` silently does nothing — so screens that want an
/// accessory publish through this instead, and `MainView` renders it.
///
/// The action is stored rather than a plain flag so the wiring stays with the feature that owns it:
/// the Recipes tab holds the navigation path a real cook-mode push would need.
@MainActor
@Observable
final class TabAccessoryModel {
    /// Non-nil while a recipe's details are on top of the Recipes tab.
    var startCooking: (() -> Void)?

    /// Non-nil while the shopping lists overview is on top of the Shopping List tab.
    var shoppingLists: ShoppingListsAccessoryContent?

    /// Non-nil while a shopping list's items are on top of the Shopping List tab.
    var addItem: AddItemAccessoryContent?

    /// Set when the accessory's search field is tapped. The accessory sits behind the keyboard
    /// while typing, so the lists screen shows the real field just above the keyboard instead and
    /// clears this once it loses focus.
    var isEditingSearch = false

    /// Same as `isEditingSearch`, for a list's add-item field.
    var isEditingAddItem = false
}

/// What the lists overview's accessory shows: the search field and the New list button. The
/// screen republishes it as its state changes; the accessory only reads it and calls back.
struct ShoppingListsAccessoryContent {
    var query: String
    var onQueryChanged: (String) -> Void
    var onNewList: () -> Void
}

/// What a list's accessory shows: the add-item field and its button.
struct AddItemAccessoryContent {
    var text: String
    var isAdding: Bool
    var onTextChanged: (String) -> Void
    var onSubmit: () -> Void
}
