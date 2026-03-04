package dev.juanrincon.simmerly.core.utils

import androidx.compose.ui.text.intl.Locale
import kotlin.math.pow
import kotlin.math.round

fun Double.format(maxFractionDigits: Int = 2): String {
    val factor = 10.0.pow(maxFractionDigits)
    val rounded = round(this * factor) / factor
    val s = rounded.toString()
    return if (s.contains('.')) s.trimEnd('0').trimEnd('.') else s
}

fun String.capitalizeWords(): String =
    replace(Regex("\\b\\p{L}")) { it.value.uppercase() }

fun String.nullIfEmpty(): String? = ifEmpty { null }