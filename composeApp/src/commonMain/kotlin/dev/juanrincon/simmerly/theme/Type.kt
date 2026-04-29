package dev.juanrincon.simmerly.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.dm_sans_italic_variablefont
import simmerly.composeapp.generated.resources.dm_sans_variablefont
import simmerly.composeapp.generated.resources.dm_serif_display_italic
import simmerly.composeapp.generated.resources.dm_serif_display_regular

@Composable
fun dmSerifDisplayFontFamily(): FontFamily {
    val regular = Font(Res.font.dm_serif_display_regular, weight = FontWeight.Normal)
    val italic =
        Font(Res.font.dm_serif_display_italic, weight = FontWeight.Normal, style = FontStyle.Italic)
    return remember(regular, italic) { FontFamily(regular, italic) }
}

@Composable
fun dmSansVariableFontFamily(): FontFamily {
    val light = Font(Res.font.dm_sans_variablefont, weight = FontWeight.Light)
    val regular = Font(Res.font.dm_sans_variablefont, weight = FontWeight.Normal)
    val italic = Font(
        Res.font.dm_sans_italic_variablefont,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    )
    val medium = Font(Res.font.dm_sans_variablefont, weight = FontWeight.Medium)
    val semiBold = Font(Res.font.dm_sans_variablefont, weight = FontWeight.SemiBold)
    val bold = Font(Res.font.dm_sans_variablefont, weight = FontWeight.Bold)
    return remember(light, regular, italic, medium, semiBold, bold) {
        FontFamily(light, regular, italic, medium, semiBold, bold)
    }
}

@Composable
fun simmerlyTypography(
    displayFontFamily: FontFamily = dmSerifDisplayFontFamily(),
    bodyFontFamily: FontFamily = dmSansVariableFontFamily()
): Typography {
    return remember(displayFontFamily, bodyFontFamily) {
        Typography(
        // --- Display — DM Serif Display ---
        displayLarge = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 57.sp,
            lineHeight = 64.sp,
            letterSpacing = (-0.25).sp
        ),
        displayMedium = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 45.sp,
            lineHeight = 52.sp,
            letterSpacing = 0.sp
        ),
        displaySmall = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 36.sp,
            lineHeight = 44.sp,
            letterSpacing = 0.sp
        ),
        // --- Headline — DM Serif Display ---
        headlineLarge = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 40.sp,
            letterSpacing = 0.sp
        ),
        headlineMedium = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 28.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp
        ),
        headlineSmall = TextStyle(
            fontFamily = displayFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 24.sp,
            lineHeight = 32.sp,
            letterSpacing = 0.sp
        ),
            // --- Title — DM Sans ---
        titleLarge = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = 0.sp
        ),
        titleMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.15.sp
        ),
        titleSmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
            // --- Body — DM Sans ---
        bodyLarge = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.5.sp
        ),
        bodyMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.25.sp
        ),
        bodySmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.4.sp
        ),
            // --- Label — DM Sans ---
        labelLarge = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            letterSpacing = 0.1.sp
        ),
        labelMedium = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        ),
        labelSmall = TextStyle(
            fontFamily = bodyFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.5.sp
        )
        )
    }
}
