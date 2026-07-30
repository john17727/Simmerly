import Foundation

/// Bottom tab bar destinations. Mirrors navigation/app/AppDestinations.kt.
enum AppTab: Hashable, CaseIterable {
    case recipes
    case mealPlan
    case shoppingList
    case profile

    var title: String {
        switch self {
        case .recipes: return String(localized: "recipes")
        case .mealPlan: return String(localized: "meal_plan")
        case .shoppingList: return String(localized: "shopping_list")
        case .profile: return String(localized: "profile")
        }
    }

    var systemImage: String {
        switch self {
        case .recipes: return "book.closed"
        case .mealPlan: return "calendar"
        case .shoppingList: return "list.bullet"
        case .profile: return "person.circle"
        }
    }
}
