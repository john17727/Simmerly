import Foundation
import Shared

extension StringKey {
    var localizationKey: String.LocalizationValue {
        switch self {
        case .loginFailed: return "login_failed"
        case .somethingWentWrong: return "something_went_wrong"
        case .unreachableServerAddress: return "unreachable_server_address"
        case .recipes: return "recipes"
        case .mealPlan: return "meal_plan"
        case .shoppingList: return "shopping_list"
        case .profile: return "profile"
        }
    }
}

extension UiText {
    /// Resolves this `UiText` against `Localizable.xcstrings`, mirroring the Compose-side
    /// `UiText.asString()` in core/presentation/UiTextResolver.kt.
    var localized: String {
        switch onEnum(of: self) {
        case .dynamic(let dynamic):
            return dynamic.text
        case .resource(let resource):
            let format = String(localized: resource.key.localizationKey)
            return resource.args.isEmpty ? format : String(format: format, arguments: resource.args)
        }
    }
}
