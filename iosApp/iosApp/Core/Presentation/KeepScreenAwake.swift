import SwiftUI
import UIKit

/// Keeps the device from auto-locking while a view is on screen. iOS counterpart to
/// core/presentation/KeepScreenOn.kt, which has no iOS `actual` since `:sharedUI` has no iOS
/// target — Cook Mode is the only screen either side needs this for.
///
/// `isIdleTimerDisabled` is a single global flag, not a per-view stack, so this only ever sets it
/// to `true` on appear and back to `false` on disappear. That is safe as long as at most one
/// screen in the app opts in at a time, which holds today.
private struct KeepScreenAwakeModifier: ViewModifier {
    func body(content: Content) -> some View {
        content
            .onAppear { UIApplication.shared.isIdleTimerDisabled = true }
            .onDisappear { UIApplication.shared.isIdleTimerDisabled = false }
    }
}

extension View {
    /// Prevents the screen from dimming/locking while this view is visible — mirrors Compose's
    /// `KeepScreenOn()` call in `CookModeScreen.kt`.
    func keepsScreenAwake() -> some View {
        modifier(KeepScreenAwakeModifier())
    }
}
