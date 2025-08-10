package dev.juanrincon.simmerly.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.nunito_italic_variablefont_wght
import simmerly.composeapp.generated.resources.nunito_variablefont_wght

@Composable
fun nunitoVariableFontFamily(): FontFamily {
    // Define FontResource objects using the generated resources
    val nunitoVariable = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Normal)
    val nunitoVariableLight = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Light)
    val nunitoVariableBold = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Bold)
    val nunitoVariableExtraBold = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.ExtraBold)
    val nunitoVariableBlack = Font(Res.font.nunito_variablefont_wght, weight = FontWeight.Black)
    val nunitoVariableItalic = Font(Res.font.nunito_italic_variablefont_wght, weight = FontWeight.Normal, style = FontStyle.Italic)

    return FontFamily(
        nunitoVariableLight,
        nunitoVariable, // Normal weight
        nunitoVariableItalic,
        nunitoVariableBold,
        nunitoVariableExtraBold,
        nunitoVariableBlack
        // You can add more specific integer weights too:
        // Font(Res.font.nunito_variablefont_wght, weight = FontWeight(200)),
        // Font(Res.font.nunito_variablefont_wght, weight = FontWeight(950)),
    )
}

// Optional: Create your Typography object using this FontFamily
@Composable
fun simmerlyTypography(fontFamily: FontFamily = nunitoVariableFontFamily()): Typography {
    val defaultTypography = Typography()
    return Typography(
        displayLarge = defaultTypography.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = defaultTypography.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = defaultTypography.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = defaultTypography.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = defaultTypography.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = defaultTypography.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = defaultTypography.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = defaultTypography.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = defaultTypography.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = defaultTypography.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = defaultTypography.labelSmall.copy(fontFamily = fontFamily)
    )
}

// Or, if you want to define specific text styles:
// @Composable
// fun simmerlyTypography(fontFamily: FontFamily = nunitoVariableFontFamily()) = Typography(
//    displayLarge = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 57.sp,
//        lineHeight = 64.sp,
//        letterSpacing = (-0.25).sp
//    ),
//    displayMedium = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 45.sp,
//        lineHeight = 52.sp,
//        letterSpacing = 0.sp
//    ),
//    displaySmall = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 36.sp,
//        lineHeight = 44.sp,
//        letterSpacing = 0.sp
//    ),
//    headlineLarge = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 32.sp,
//        lineHeight = 40.sp,
//        letterSpacing = 0.sp
//    ),
//    headlineMedium = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 28.sp,
//        lineHeight = 36.sp,
//        letterSpacing = 0.sp
//    ),
//    headlineSmall = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 24.sp,
//        lineHeight = 32.sp,
//        letterSpacing = 0.sp
//    ),
//    titleLarge = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Bold, // Material 3 uses Normal, but Bold is often preferred for titles
//        fontSize = 22.sp,
//        lineHeight = 28.sp,
//        letterSpacing = 0.sp
//    ),
//    titleMedium = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Bold, // Material 3 uses Medium
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.15.sp
//    ),
//    titleSmall = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Bold, // Material 3 uses Medium
//        fontSize = 14.sp,
//        lineHeight = 20.sp,
//        letterSpacing = 0.1.sp
//    ),
//    bodyLarge = TextStyle(
//        fontFamily = fontFamily,
//        fontWeight = FontWeight.Normal,
//        fontSize = 16.sp,
//        lineHeight = 24.sp,
//        letterSpacing = 0.5.sp
//    ),
//    // ... and so on for bodyMedium, bodySmall, labelLarge, labelMedium, labelSmall
//    // You can find the default values in the Material 3 documentation:
//    // https://m3.material.io/styles/typography/type-scale-tokens
// )

