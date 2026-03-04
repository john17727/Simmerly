package dev.juanrincon.simmerly.core.presentation

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicText(val text: String) : UiText
    data class StringResText(val res: StringResource, val args: List<Any> = emptyList()) : UiText

    @Composable
    fun asString(): String = when (this) {
        is DynamicText -> text
        is StringResText -> stringResource(res, *args.toTypedArray())
    }
}