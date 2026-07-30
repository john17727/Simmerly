package dev.juanrincon.simmerly.core.utils

import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.round

fun Double.format(maxFractionDigits: Int = 2): String {
    val factor = 10.0.pow(maxFractionDigits)
    val rounded = round(this * factor) / factor
    val s = rounded.toString()
    return if (s.contains('.')) s.trimEnd('0').trimEnd('.') else s
}

fun Double.toFractionString(): String {
    val commonFractions = listOf(
        0.125 to "1/8",
        1.0 / 6 to "1/6",
        0.25 to "1/4",
        1.0 / 3 to "1/3",
        0.375 to "3/8",
        0.5 to "1/2",
        0.625 to "5/8",
        2.0 / 3 to "2/3",
        0.75 to "3/4",
        0.875 to "7/8"
    )
    val whole = floor(this).toInt()
    val frac = this - whole
    val tolerance = 0.04
    val fracStr = commonFractions.firstOrNull { abs(it.first - frac) < tolerance }?.second
    return when {
        fracStr != null && whole > 0 -> "$whole $fracStr"
        fracStr != null -> fracStr
        whole > 0 -> whole.toString()
        else -> this.format(2)
    }
}

fun String.capitalizeWords(): String =
    replace(Regex("\\b\\p{L}")) { it.value.uppercase() }

fun String.nullIfEmpty(): String? = ifEmpty { null }