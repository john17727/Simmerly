package dev.juanrincon.simmerly.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import simmerly.composeapp.generated.resources.DMSerifDisplay_Italic
import simmerly.composeapp.generated.resources.DMSerifDisplay_Regular
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.nunito_italic_variablefont_wght
import simmerly.composeapp.generated.resources.nunito_variablefont_wght

@Composable
fun dmSerifDisplayFontFamily(): FontFamily {
    val dmSerifDisplayRegular = Font(Res.font.DMSerifDisplay_Regular, weight = FontWeight.Normal)
    val dmSerifDisplayItalic =
        Font(Res.font.DMSerifDisplay_Italic, weight = FontWeight.Normal, style = FontStyle.Italic)

    return FontFamily(
        dmSerifDisplayRegular,
        dmSerifDisplayItalic
    )
}

@Composable
fun nunitoVariableFontFamily(): FontFamily {
    val nunitoVariableLight = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Light)
    val nunitoVariable = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Normal)
    val nunitoVariableItalic = Font(
        Res.font.nunito_italic_variablefont_wght,
        weight = FontWeight.Normal,
        style = FontStyle.Italic
    )
    val nunitoVariableMedium = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Medium)
    val nunitoVariableSemiBold =
        Font(Res.font.nunito_variablefont_wght, weight = FontWeight.SemiBold)
    val nunitoVariableBold = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Bold)
    val nunitoVariableExtraBold = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.ExtraBold)
    val nunitoVariableBlack = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Black)

    return FontFamily(
        nunitoVariableLight,
        nunitoVariable,
        nunitoVariableItalic,
        nunitoVariableMedium,
        nunitoVariableSemiBold,
        nunitoVariableBold,
        nunitoVariableExtraBold,
        nunitoVariableBlack
    )
}

@Composable
fun simmerlyTypography(
    displayFontFamily: FontFamily = dmSerifDisplayFontFamily(),
    bodyFontFamily: FontFamily = nunitoVariableFontFamily()
): Typography {
    return Typography(
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
        // --- Title — Nunito ---
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
        // --- Body — Nunito ---
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
        // --- Label — Nunito ---
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

