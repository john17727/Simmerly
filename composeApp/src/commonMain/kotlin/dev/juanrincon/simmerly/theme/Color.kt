package dev.juanrincon.simmerly.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Optional: keep your brand colors in one place
object Simmerly {
    val Coral = Color(0xFFFF7C6A)   // Primary
    val CoralPressed = Color(0xFFFF6A55)
    val CoralSubtle = Color(0xFFFFE8E3)
    val TextPrimary = Color(0xFF1C1D20)
    val TextSecondary = Color(0xFF6E7177)
    val Peach = Color(0xFFFFB8A8)   // Tertiary accent (rarely used)
    val Card = Color(0xFFF6F7F9)
    val Divider = Color(0xFFE5E7EA)
    val Outline = Color(0xFFC9CDD3)
    val Canvas = Color(0xFFFFFFFF)
}

// LIGHT THEME
val SimmerlyLightColorScheme = lightColorScheme(
    primary = Simmerly.Coral,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Simmerly.CoralSubtle,
    onPrimaryContainer = Color(0xFF7A2C22),

    secondary = Simmerly.TextSecondary,          // subtle interactive/secondary
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Simmerly.Divider,
    onSecondaryContainer = Simmerly.TextPrimary,

    tertiary = Simmerly.Peach,
    onTertiary = Color(0xFF5B2018),
    tertiaryContainer = Color(0xFFFFE1DB),
    onTertiaryContainer = Color(0xFF6E261C),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Simmerly.Canvas,
    onBackground = Simmerly.TextPrimary,

    surface = Simmerly.Canvas,
    onSurface = Simmerly.TextPrimary,
    surfaceVariant = Simmerly.Card,
    onSurfaceVariant = Simmerly.TextSecondary,

    outline = Simmerly.Outline,
    outlineVariant = Simmerly.Divider,

    inverseSurface = Color(0xFF2C2D31),
    inverseOnSurface = Color(0xFFF3F4F6),
    inversePrimary = Color(0xFFFFB4A8),

    scrim = Color(0xFF000000),
    surfaceTint = Simmerly.Coral
)

// DARK THEME
val SimmerlyDarkColorScheme = darkColorScheme(
    primary = Color(0xFFFFB4A8),
    onPrimary = Color(0xFF5B1A14),
    primaryContainer = Color(0xFF7C2D23),
    onPrimaryContainer = Color(0xFFFFDAD3),

    secondary = Color(0xFFBFC3CA),
    onSecondary = Color(0xFF2E3136),
    secondaryContainer = Color(0xFF43474E),
    onSecondaryContainer = Color(0xFFDDE2E8),

    tertiary = Color(0xFFFFD0C5),
    onTertiary = Color(0xFF5B2018),
    tertiaryContainer = Color(0xFF7C2D23),
    onTertiaryContainer = Color(0xFFFFDAD3),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF111315),
    onBackground = Color(0xFFE5E7EA),

    surface = Color(0xFF111315),
    onSurface = Color(0xFFEDEFF2),
    surfaceVariant = Color(0xFF2A2D32),
    onSurfaceVariant = Color(0xFFC9CDD3),

    outline = Color(0xFF8E939B),
    outlineVariant = Color(0xFF3A3E45),

    inverseSurface = Color(0xFFE5E7EA),
    inverseOnSurface = Color(0xFF1C1D20),
    inversePrimary = Simmerly.Coral,

    scrim = Color(0xFF000000),
    surfaceTint = Color(0xFFFFB4A8)
)
