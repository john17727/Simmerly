package dev.juanrincon.simmerly.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object Simmerly {
    // Primary — coral salmon (#FF7C6A seed)
    val Coral = Color(0xFFFF7C6A)          // --color-primary-400
    val CoralPressed = Color(0xFFF5624E)   // --color-primary-500
    val CoralSubtle = Color(0xFFFFF0EE)    // --color-primary-50
    val CoralMuted = Color(0xFFFFD8D3)     // --color-primary-100

    // Foreground
    val TextPrimary = Color(0xFF17191C)    // --fg-primary / --color-neutral-900
    val TextSecondary = Color(0xFF5F6269)  // --fg-secondary / --color-neutral-600
    val TextMuted = Color(0xFF828590)      // --fg-muted / --color-neutral-500
    val TextDisabled = Color(0xFFC8C9CE)   // --fg-disabled / --color-neutral-300

    // Secondary — Dusty Rose
    val DustyRose = Color(0xFFC2857A)      // --color-secondary-400

    // Tertiary — Herb Sage
    val HerbSage = Color(0xFF5E9E74)       // --color-tertiary-400

    // Surfaces
    val Surface1 = Color(0xFFFFFFFF)       // --surface-1 / --color-neutral-0
    val Surface2 = Color(0xFFF9F9FA)       // --surface-2 / --color-neutral-50
    val Surface3 = Color(0xFFF2F2F4)       // --surface-3 / --color-neutral-100
    val Background = Color(0xFFF9F9FA)     // --bg-subtle / --color-neutral-50

    // Borders
    val BorderDefault = Color(0xFFE2E3E6)  // --border-default / --color-neutral-200
    val BorderStrong = Color(0xFFA8AAB0)   // --border-strong / --color-neutral-400
}

// LIGHT THEME
val SimmerlyLightColorScheme = lightColorScheme(
    primary = Simmerly.Coral,                       // --color-primary-400
    onPrimary = Color(0xFFFFFFFF),                  // --color-neutral-0
    primaryContainer = Simmerly.CoralMuted,         // --color-primary-100
    onPrimaryContainer = Color(0xFF75130D),         // --color-primary-900

    secondary = Simmerly.DustyRose,                 // --color-secondary-400
    onSecondary = Color(0xFFFFFFFF),                // --color-neutral-0
    secondaryContainer = Color(0xFFF9EEEC),         // --color-secondary-50
    onSecondaryContainer = Color(0xFF5A2A24),       // --color-secondary-800

    tertiary = Simmerly.HerbSage,                   // --color-tertiary-400
    onTertiary = Color(0xFFFFFFFF),                 // --color-neutral-0
    tertiaryContainer = Color(0xFFEDF3EF),          // --color-tertiary-50
    onTertiaryContainer = Color(0xFF1C3D2A),        // --color-tertiary-800

    error = Color(0xFFD93025),                      // --color-error-500
    onError = Color(0xFFFFFFFF),                    // --color-neutral-0
    errorContainer = Color(0xFFFDECEA),             // --color-error-100
    onErrorContainer = Color(0xFF9B1C14),           // --color-error-700

    background = Simmerly.Background,               // --bg-subtle / --color-neutral-50
    onBackground = Simmerly.TextPrimary,            // --fg-primary / --color-neutral-900

    surface = Simmerly.Surface1,                    // --bg-base / --color-neutral-0
    onSurface = Simmerly.TextPrimary,               // --fg-primary / --color-neutral-900
    surfaceVariant = Simmerly.Surface3,             // --bg-muted / --color-neutral-100
    onSurfaceVariant = Simmerly.TextSecondary,      // --fg-secondary / --color-neutral-600
    surfaceContainerLowest = Simmerly.Surface1,     // --color-neutral-0
    surfaceContainerLow = Simmerly.Surface2,        // --color-neutral-50
    surfaceContainer = Simmerly.Surface3,           // --color-neutral-100
    surfaceContainerHigh = Simmerly.BorderDefault,  // --color-neutral-200
    surfaceContainerHighest = Simmerly.TextDisabled,// --color-neutral-300

    outline = Simmerly.BorderStrong,                // --border-strong / --color-neutral-400
    outlineVariant = Simmerly.BorderDefault,        // --border-default / --color-neutral-200

    inverseSurface = Color(0xFF282A2E),             // --bg-emphasis / --color-neutral-800
    inverseOnSurface = Simmerly.Surface2,           // --color-neutral-50
    inversePrimary = Color(0xFFFFB3A7),             // --color-primary-200

    scrim = Color(0xFF000000),
    surfaceTint = Simmerly.Coral                    // --brand / --color-primary-400
)

// DARK THEME
val SimmerlyDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB3A7),                    // --color-primary-200
    onPrimary = Color(0xFF75130D),                  // --color-primary-900
    primaryContainer = Color(0xFF9E1E14),           // --color-primary-800
    onPrimaryContainer = Simmerly.CoralMuted,       // --color-primary-100

    secondary = Color(0xFFE8C4BE),                  // --color-secondary-200
    onSecondary = Color(0xFF5A2A24),                // --color-secondary-800
    secondaryContainer = Color(0xFF8F4F46),         // --color-secondary-600
    onSecondaryContainer = Color(0xFFF9EEEC),       // --color-secondary-50

    tertiary = Color(0xFFAECDB7),                   // --color-tertiary-200
    onTertiary = Color(0xFF1C3D2A),                 // --color-tertiary-800
    tertiaryContainer = Color(0xFF366B4A),          // --color-tertiary-600
    onTertiaryContainer = Color(0xFFEDF3EF),        // --color-tertiary-50

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFDECEA),           // --color-error-100

    background = Color(0xFF17191C),                 // --color-neutral-900
    onBackground = Color(0xFFF2F2F4),               // --color-neutral-100

    surface = Color(0xFF17191C),                    // --color-neutral-900
    onSurface = Color(0xFFF2F2F4),                  // --color-neutral-100
    surfaceVariant = Color(0xFF404347),             // --color-neutral-700
    onSurfaceVariant = Simmerly.TextDisabled,       // --color-neutral-300
    surfaceContainerLowest = Color(0xFF17191C),     // --color-neutral-900
    surfaceContainerLow = Color(0xFF282A2E),        // --color-neutral-800
    surfaceContainer = Color(0xFF404347),           // --color-neutral-700
    surfaceContainerHigh = Color(0xFF5F6269),       // --color-neutral-600
    surfaceContainerHighest = Simmerly.TextMuted,   // --color-neutral-500

    outline = Simmerly.TextMuted,                   // --color-neutral-500
    outlineVariant = Color(0xFF404347),             // --color-neutral-700

    inverseSurface = Color(0xFFF2F2F4),             // --color-neutral-100
    inverseOnSurface = Color(0xFF17191C),           // --color-neutral-900
    inversePrimary = Simmerly.Coral,                // --color-primary-400 (seed)

    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFB3A7)                 // --color-primary-200
)
