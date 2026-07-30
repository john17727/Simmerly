import Foundation

/// NavigationStack path for the recipes tab. Mirrors recipes/presentation/navigation/RecipeDestinations.kt
/// (List is the tab root and isn't pushed onto the stack).
enum AppRoute: Hashable {
    case detail(recipeId: String)
    case comments(recipeId: String)
    case search
}
