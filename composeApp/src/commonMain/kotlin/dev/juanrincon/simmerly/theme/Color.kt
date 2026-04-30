package dev.juanrincon.simmerly.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Simmerly {
    // ── Coral — coral salmon ────────────────────────────────────────────────
    val Coral50 = Color(0xFFFFF0EE)  // --color-primary-50
    val Coral100 = Color(0xFFFFD8D3)  // --color-primary-100
    val Coral200 = Color(0xFFFFB3A7)  // --color-primary-200
    val Coral300 = Color(0xFFFF9587)  // --color-primary-300
    val Coral400 = Color(0xFFFF7C6A)  // --color-primary-400 (seed)
    val Coral500 = Color(0xFFF5624E)  // --color-primary-500
    val Coral600 = Color(0xFFE04437)  // --color-primary-600
    val Coral700 = Color(0xFFC42E22)  // --color-primary-700
    val Coral800 = Color(0xFF9E1E14)  // --color-primary-800
    val Coral900 = Color(0xFF75130D)  // --color-primary-900

    // ── Neutral — steel cool ──────────────────────────────────────────────────
    val Neutral0 = Color(0xFFFFFFFF)  // --color-neutral-0   (--surface-1 / --bg-base)
    val Neutral50 = Color(0xFFF9F9FA)  // --color-neutral-50  (--surface-2 / --bg-subtle)
    val Neutral100 = Color(0xFFF2F2F4)  // --color-neutral-100 (--surface-3 / --bg-muted)
    val Neutral200 = Color(0xFFE2E3E6)  // --color-neutral-200 (--border-default)
    val Neutral300 = Color(0xFFC8C9CE)  // --color-neutral-300 (--fg-disabled)
    val Neutral400 = Color(0xFFA8AAB0)  // --color-neutral-400 (--border-strong)
    val Neutral500 = Color(0xFF828590)  // --color-neutral-500 (--fg-muted)
    val Neutral600 = Color(0xFF5F6269)  // --color-neutral-600 (--fg-secondary)
    val Neutral700 = Color(0xFF404347)  // --color-neutral-700
    val Neutral800 = Color(0xFF282A2E)  // --color-neutral-800 (--bg-emphasis)
    val Neutral900 = Color(0xFF17191C)  // --color-neutral-900 (--fg-primary)
    val Black = Color(0xFF000000)

    // ── Secondary — Dusty Rose ────────────────────────────────────────────────
    val DustyRoseSubtle = Color(0xFFF9EEEC)  // --color-secondary-50
    val DustyRoseLight = Color(0xFFE8C4BE)  // --color-secondary-200
    val DustyRose = Color(0xFFC2857A)  // --color-secondary-400
    val DustyRoseDark = Color(0xFF8F4F46)  // --color-secondary-600
    val DustyRoseDeep = Color(0xFF5A2A24)  // --color-secondary-800

    // ── Tertiary — Herb Sage ──────────────────────────────────────────────────
    val HerbSageSubtle = Color(0xFFEDF3EF)  // --color-tertiary-50
    val HerbSageLight = Color(0xFFAECDB7)  // --color-tertiary-200
    val HerbSage = Color(0xFF5E9E74)  // --color-tertiary-400
    val HerbSageDark = Color(0xFF366B4A)  // --color-tertiary-600
    val HerbSageDeep = Color(0xFF1C3D2A)  // --color-tertiary-800

    // ── Error ─────────────────────────────────────────────────────────────────
    val ErrorSubtle = Color(0xFFFDECEA)  // --color-error-100
    val ErrorRed = Color(0xFFD93025)  // --color-error-500
    val ErrorDark = Color(0xFF9B1C14)  // --color-error-700
    val ErrorDarkPrimary = Color(0xFFFFB4AB)  // M3 dark-theme error
    val ErrorDarkOn = Color(0xFF690005)  // M3 dark-theme on-error
    val ErrorDarkContainer = Color(0xFF93000A)  // M3 dark-theme error-container
}

// LIGHT THEME
val SimmerlyLightColorScheme = lightColorScheme(
    primary = Simmerly.Coral400,
    onPrimary = Simmerly.Neutral0,
    primaryContainer = Simmerly.Coral100,
    onPrimaryContainer = Simmerly.Coral900,

    secondary = Simmerly.DustyRose,
    onSecondary = Simmerly.Neutral0,
    secondaryContainer = Simmerly.DustyRoseSubtle,
    onSecondaryContainer = Simmerly.DustyRoseDeep,

    tertiary = Simmerly.HerbSage,
    onTertiary = Simmerly.Neutral0,
    tertiaryContainer = Simmerly.HerbSageSubtle,
    onTertiaryContainer = Simmerly.HerbSageDeep,

    error = Simmerly.ErrorRed,
    onError = Simmerly.Neutral0,
    errorContainer = Simmerly.ErrorSubtle,
    onErrorContainer = Simmerly.ErrorDark,

    background = Simmerly.Neutral0,
    onBackground = Simmerly.Neutral900,

    surface = Simmerly.Neutral0,
    onSurface = Simmerly.Neutral900,
    surfaceVariant = Simmerly.Neutral100,
    onSurfaceVariant = Simmerly.Neutral600,
    surfaceContainerLowest = Simmerly.Neutral0,
    surfaceContainerLow = Simmerly.Neutral50,
    surfaceContainer = Simmerly.Neutral100,
    surfaceContainerHigh = Simmerly.Neutral200,
    surfaceContainerHighest = Simmerly.Neutral300,

    outline = Simmerly.Neutral400,
    outlineVariant = Simmerly.Neutral200,

    inverseSurface = Simmerly.Neutral800,
    inverseOnSurface = Simmerly.Neutral50,
    inversePrimary = Simmerly.Coral200,

    scrim = Simmerly.Black,
    surfaceTint = Simmerly.Coral400
)

// DARK THEME
val SimmerlyDarkColorScheme = darkColorScheme(
    primary = Simmerly.Coral200,
    onPrimary = Simmerly.Coral900,
    primaryContainer = Simmerly.Coral800,
    onPrimaryContainer = Simmerly.Coral100,

    secondary = Simmerly.DustyRoseLight,
    onSecondary = Simmerly.DustyRoseDeep,
    secondaryContainer = Simmerly.DustyRoseDark,
    onSecondaryContainer = Simmerly.DustyRoseSubtle,

    tertiary = Simmerly.HerbSageLight,
    onTertiary = Simmerly.HerbSageDeep,
    tertiaryContainer = Simmerly.HerbSageDark,
    onTertiaryContainer = Simmerly.HerbSageSubtle,

    error = Simmerly.ErrorDarkPrimary,
    onError = Simmerly.ErrorDarkOn,
    errorContainer = Simmerly.ErrorDarkContainer,
    onErrorContainer = Simmerly.ErrorSubtle,

    background = Simmerly.Neutral900,
    onBackground = Simmerly.Neutral100,

    surface = Simmerly.Neutral900,
    onSurface = Simmerly.Neutral100,
    surfaceVariant = Simmerly.Neutral700,
    onSurfaceVariant = Simmerly.Neutral300,
    surfaceContainerLowest = Simmerly.Neutral900,
    surfaceContainerLow = Simmerly.Neutral800,
    surfaceContainer = Simmerly.Neutral700,
    surfaceContainerHigh = Simmerly.Neutral600,
    surfaceContainerHighest = Simmerly.Neutral500,

    outline = Simmerly.Neutral500,
    outlineVariant = Simmerly.Neutral700,

    inverseSurface = Simmerly.Neutral100,
    inverseOnSurface = Simmerly.Neutral900,
    inversePrimary = Simmerly.Coral400,

    scrim = Simmerly.Black,
    surfaceTint = Simmerly.Coral200
)
