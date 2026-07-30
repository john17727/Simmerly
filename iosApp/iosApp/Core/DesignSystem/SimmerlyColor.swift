import SwiftUI
import UIKit

extension Color {
    init(hex: UInt32) {
        self.init(
            .sRGB,
            red: Double((hex >> 16) & 0xFF) / 255,
            green: Double((hex >> 8) & 0xFF) / 255,
            blue: Double(hex & 0xFF) / 255,
            opacity: 1
        )
    }

    init(light: Color, dark: Color) {
        self.init(UIColor { traits in
            traits.userInterfaceStyle == .dark ? UIColor(dark) : UIColor(light)
        })
    }
}

/// Raw design tokens, ported from theme/Color.kt. Prefer `SimmerlyColor`'s semantic roles in views.
enum SimmerlyPalette {
    // Coral — coral salmon
    static let coral50 = Color(hex: 0xFFF0EE)
    static let coral100 = Color(hex: 0xFFD8D3)
    static let coral200 = Color(hex: 0xFFB3A7)
    static let coral400 = Color(hex: 0xFF7C6A)
    static let coral600 = Color(hex: 0xE04437)
    static let coral700 = Color(hex: 0xC42E22)
    static let coral900 = Color(hex: 0x75130D)

    // Neutral — steel cool
    static let neutral0 = Color(hex: 0xFFFFFF)
    static let neutral50 = Color(hex: 0xF9F9FA)
    static let neutral100 = Color(hex: 0xF2F2F4)
    static let neutral200 = Color(hex: 0xE2E3E6)
    static let neutral300 = Color(hex: 0xC8C9CE)
    static let neutral400 = Color(hex: 0xA8AAB0)
    static let neutral500 = Color(hex: 0x828590)
    static let neutral600 = Color(hex: 0x5F6269)
    static let neutral700 = Color(hex: 0x404347)
    static let neutral800 = Color(hex: 0x282A2E)
    static let neutral900 = Color(hex: 0x17191C)
    static let black = Color(hex: 0x000000)

    // Tertiary — Herb Sage
    static let herbSageSubtle = Color(hex: 0xEDF3EF)
    static let herbSageLight = Color(hex: 0xAECDB7)
    static let herbSage = Color(hex: 0x5E9E74)
    static let herbSageDark = Color(hex: 0x366B4A)
    static let herbSageDeep = Color(hex: 0x1C3D2A)

    // Error
    static let errorSubtle = Color(hex: 0xFDECEA)
    static let errorRed = Color(hex: 0xD93025)
    static let errorDark = Color(hex: 0x9B1C14)
    static let errorDarkPrimary = Color(hex: 0xFFB4AB)
    static let errorDarkOn = Color(hex: 0x690005)
    static let errorDarkContainer = Color(hex: 0x93000A)
}

/// Semantic color roles, mirroring the Material 3 light/dark schemes in theme/Color.kt.
/// Each role switches automatically with the system appearance.
enum SimmerlyColor {
    static let primary = Color(light: SimmerlyPalette.coral400, dark: SimmerlyPalette.coral200)
    static let onPrimary = Color(light: SimmerlyPalette.neutral0, dark: SimmerlyPalette.coral900)
    static let primaryContainer = Color(light: SimmerlyPalette.coral100, dark: SimmerlyPalette.coral600)
    static let onPrimaryContainer = Color(light: SimmerlyPalette.coral900, dark: SimmerlyPalette.coral100)

    static let secondary = Color(light: SimmerlyPalette.coral600, dark: SimmerlyPalette.coral100)
    static let onSecondary = Color(light: SimmerlyPalette.neutral0, dark: SimmerlyPalette.coral900)
    static let secondaryContainer = Color(light: SimmerlyPalette.coral100, dark: SimmerlyPalette.coral700)
    static let onSecondaryContainer = Color(light: SimmerlyPalette.coral600, dark: SimmerlyPalette.coral200)

    static let tertiary = Color(light: SimmerlyPalette.herbSage, dark: SimmerlyPalette.herbSageLight)
    static let onTertiary = Color(light: SimmerlyPalette.neutral0, dark: SimmerlyPalette.herbSageDeep)
    static let tertiaryContainer = Color(light: SimmerlyPalette.herbSageSubtle, dark: SimmerlyPalette.herbSageDark)
    static let onTertiaryContainer = Color(light: SimmerlyPalette.herbSageDeep, dark: SimmerlyPalette.herbSageSubtle)

    static let error = Color(light: SimmerlyPalette.errorRed, dark: SimmerlyPalette.errorDarkPrimary)
    static let onError = Color(light: SimmerlyPalette.neutral0, dark: SimmerlyPalette.errorDarkOn)
    static let errorContainer = Color(light: SimmerlyPalette.errorSubtle, dark: SimmerlyPalette.errorDarkContainer)
    static let onErrorContainer = Color(light: SimmerlyPalette.errorDark, dark: SimmerlyPalette.errorSubtle)

    static let background = Color(light: SimmerlyPalette.neutral0, dark: SimmerlyPalette.neutral900)
    static let onBackground = Color(light: SimmerlyPalette.neutral900, dark: SimmerlyPalette.neutral100)

    static let surface = Color(light: SimmerlyPalette.neutral0, dark: SimmerlyPalette.neutral900)
    static let onSurface = Color(light: SimmerlyPalette.neutral900, dark: SimmerlyPalette.neutral100)
    static let onSurfaceVariant = Color(light: SimmerlyPalette.neutral600, dark: SimmerlyPalette.neutral300)

    static let surfaceContainerLow = Color(light: SimmerlyPalette.neutral50, dark: SimmerlyPalette.neutral800)
    static let surfaceContainer = Color(light: SimmerlyPalette.neutral100, dark: SimmerlyPalette.neutral700)
    static let surfaceContainerHigh = Color(light: SimmerlyPalette.neutral200, dark: SimmerlyPalette.neutral600)
    static let surfaceContainerHighest = Color(light: SimmerlyPalette.neutral300, dark: SimmerlyPalette.neutral500)

    static let outline = Color(light: SimmerlyPalette.neutral400, dark: SimmerlyPalette.neutral500)
    static let outlineVariant = Color(light: SimmerlyPalette.neutral200, dark: SimmerlyPalette.neutral700)
}
