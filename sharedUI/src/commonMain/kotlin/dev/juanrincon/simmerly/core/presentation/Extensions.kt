package dev.juanrincon.simmerly.core.presentation

import androidx.compose.ui.Modifier

inline fun Modifier.ifTrue(condition: Boolean, builder: Modifier.() -> Modifier): Modifier =
    if (condition) {
        builder(this)
    } else {
        this
    }

inline fun Modifier.ifElse(
    condition: Boolean,
    ifTrue: Modifier.() -> Modifier,
    ifFalse: Modifier.() -> Modifier
): Modifier = if (condition) {
    ifTrue(this)
} else {
    ifFalse(this)
}