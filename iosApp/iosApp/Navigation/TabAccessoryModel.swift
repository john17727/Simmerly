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
}
