package dev.juanrincon.simmerly.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import simmerly.shared.generated.resources.Res
import simmerly.shared.generated.resources.login_failed
import simmerly.shared.generated.resources.meal_plan
import simmerly.shared.generated.resources.profile
import simmerly.shared.generated.resources.recipes
import simmerly.shared.generated.resources.shopping_list
import simmerly.shared.generated.resources.something_went_wrong
import simmerly.shared.generated.resources.unreachable_server_address

@Composable
fun UiText.asString(): String = when (this) {
    is UiText.Dynamic -> text
    is UiText.Resource -> stringResource(key.toStringResource(), *args.toTypedArray())
}

suspend fun UiText.asStringSuspend(): String = when (this) {
    is UiText.Dynamic -> text
    is UiText.Resource -> getString(key.toStringResource(), *args.toTypedArray())
}

private fun StringKey.toStringResource(): StringResource = when (this) {
    StringKey.LoginFailed -> Res.string.login_failed
    StringKey.SomethingWentWrong -> Res.string.something_went_wrong
    StringKey.UnreachableServerAddress -> Res.string.unreachable_server_address
    StringKey.Recipes -> Res.string.recipes
    StringKey.MealPlan -> Res.string.meal_plan
    StringKey.ShoppingList -> Res.string.shopping_list
    StringKey.Profile -> Res.string.profile
}
