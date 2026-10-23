import SwiftUI

/// A short message shown at the bottom of a screen, optionally with one action — the iOS
/// counterpart of Material's Snackbar, which the Compose screens use for errors and Undo.
struct SnackbarMessage: Identifiable, Equatable {
    let id = UUID()
    let text: String
    var actionLabel: String? = nil
    var action: (() -> Void)? = nil

    static func == (lhs: SnackbarMessage, rhs: SnackbarMessage) -> Bool { lhs.id == rhs.id }
}

extension View {
    /// Shows `message` above the bottom edge until it times out (longer when it carries an action,
    /// so there's time to reach Undo) or its action is tapped. Setting a new message replaces the
    /// current one.
    func snackbar(_ message: Binding<SnackbarMessage?>) -> some View {
        overlay(alignment: .bottom) {
            if let current = message.wrappedValue {
                SnackbarView(message: current, onDismiss: { message.wrappedValue = nil })
                    .padding(.horizontal, 16)
                    .padding(.bottom, 12)
                    .transition(.move(edge: .bottom).combined(with: .opacity))
                    .task(id: current.id) {
                        try? await Task.sleep(for: .seconds(current.action == nil ? 4 : 6))
                        if message.wrappedValue?.id == current.id { message.wrappedValue = nil }
                    }
            }
        }
        .animation(.snappy, value: message.wrappedValue)
    }
}

private struct SnackbarView: View {
    let message: SnackbarMessage
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            Text(message.text)
                .font(SimmerlyFont.bodyMedium)
                .foregroundStyle(SimmerlyColor.inverseOnSurface)
                .frame(maxWidth: .infinity, alignment: .leading)
            if let label = message.actionLabel, let action = message.action {
                Button(label) {
                    action()
                    onDismiss()
                }
                .font(SimmerlyFont.titleSmall)
                .foregroundStyle(SimmerlyColor.inversePrimary)
                .frame(minHeight: 40)
            }
        }
        .padding(.leading, 16)
        .padding(.trailing, message.action == nil ? 16 : 8)
        .padding(.vertical, 4)
        .frame(minHeight: 48)
        .background {
            RoundedRectangle(cornerRadius: 8)
                .fill(SimmerlyColor.inverseSurface)
                .shadow(color: .black.opacity(0.18), radius: 6, y: 4)
        }
        .accessibilityElement(children: .combine)
        .accessibilityAddTraits(.updatesFrequently)
    }
}

#Preview {
    @Previewable @State var message: SnackbarMessage? = SnackbarMessage(
        text: "12 items checked",
        actionLabel: "Undo",
        action: {}
    )
    Color.clear
        .snackbar($message)
}
