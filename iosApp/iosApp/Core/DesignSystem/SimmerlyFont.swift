import SwiftUI

/// Text styles ported from theme/Type.kt. Display/Headline/TitleLarge use DM Serif Display;
/// everything else uses the DM Sans variable font (weight comes from `.fontWeight`/`.weight`,
/// which CoreText interpolates against the font's `wght` axis).
enum SimmerlyFont {
    private static let displayFamily = "DM Serif Display"
    private static let sansFamily = "DM Sans 9pt"

    static let displayLarge = Font.custom(displayFamily, size: 57)
    static let displayMedium = Font.custom(displayFamily, size: 45)
    static let displaySmall = Font.custom(displayFamily, size: 36)

    static let headlineLarge = Font.custom(displayFamily, size: 32)
    static let headlineMedium = Font.custom(displayFamily, size: 28)
    static let headlineSmall = Font.custom(displayFamily, size: 24)

    static let titleLarge = Font.custom(displayFamily, size: 22)
    static let titleMedium = Font.custom(sansFamily, size: 16).weight(.semibold)
    static let titleSmall = Font.custom(sansFamily, size: 14).weight(.semibold)

    static let bodyLarge = Font.custom(sansFamily, size: 16)
    static let bodyMedium = Font.custom(sansFamily, size: 14)
    static let bodySmall = Font.custom(sansFamily, size: 12)

    static let labelLarge = Font.custom(sansFamily, size: 14).weight(.medium)
    static let labelMedium = Font.custom(sansFamily, size: 12).weight(.medium)
    static let labelSmall = Font.custom(sansFamily, size: 11).weight(.medium)
}
