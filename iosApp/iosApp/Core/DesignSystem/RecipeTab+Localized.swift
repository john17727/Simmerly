import Shared

extension RecipeTab {
    /// Mirrors recipes/presentation/details/orbit/RecipeTab.kt's `label` extension.
    var label: String {
        switch self {
        case .overview: return "Overview"
        case .ingredients: return "Ingredients"
        case .instructions: return "Instructions"
        case .notes: return "Notes"
        case .nutrition: return "Nutrition"
        case .recipe: return "Recipe"
        case .comments: return "Comments"
        }
    }
}
