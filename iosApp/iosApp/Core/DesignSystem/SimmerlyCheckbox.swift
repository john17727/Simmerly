import SwiftUI

/// The rounded-square checkbox used by checkable rows: cook mode's mise en place and shopping list
/// items. Purely visual — the row around it owns the tap and the accessibility trait.
struct SimmerlyCheckbox: View {
    let checked: Bool

    var body: some View {
        ZStack {
            RoundedRectangle(cornerRadius: 4)
                .fill(checked ? SimmerlyColor.primary : Color.clear)
            RoundedRectangle(cornerRadius: 4)
                .strokeBorder(checked ? Color.clear : SimmerlyColor.outline, lineWidth: 2)
            if checked {
                Image(systemName: "checkmark")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundStyle(SimmerlyColor.onPrimary)
            }
        }
        .frame(width: 22, height: 22)
        .accessibilityHidden(true)
    }
}

#Preview {
    HStack(spacing: 16) {
        SimmerlyCheckbox(checked: false)
        SimmerlyCheckbox(checked: true)
    }
    .padding()
}
