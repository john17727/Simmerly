import SwiftUI

/// Primary call to action for the recipe detail screen, rendered in the tab view's bottom
/// accessory area rather than the navigation bar.
///
/// The accessory owns the whole row above the tab bar at full size, and shares it with a capsule
/// once `tabBarMinimizeBehavior(.onScrollDown)` collapses the bar. Both are handled by filling
/// whatever width the container offers, so this doesn't need to read the placement.
struct StartCookingAccessory: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text("Start Cooking")
                .font(SimmerlyFont.labelLarge)
                // The system would pick white here, which is unreadable in dark mode where
                // `primary` resolves to a light coral. `onPrimary` is the design system's answer.
                .foregroundStyle(SimmerlyColor.onPrimary)
                // Fill the accessory rather than sitting a second pill inside it: the container
                // draws its own glass capsule, and a bordered button leaves that glass showing
                // as a ring around the fill. Nothing tints or suppresses the container, so the
                // content has to cover it.
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(SimmerlyColor.primary)
                .contentShape(Rectangle())
        }
        .buttonStyle(FilledAccessoryButtonStyle())
        .accessibilityLabel("Start Cooking")
    }
}

/// Filling the accessory means giving up `.borderedProminent`, and with it its press feedback —
/// a plain button leaves a primary call to action looking inert on tap. This restores the dim.
private struct FilledAccessoryButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .opacity(configuration.isPressed ? 0.75 : 1)
            .animation(.easeOut(duration: 0.15), value: configuration.isPressed)
    }
}

// MARK: - Previews

#Preview {
    TabView {
        Tab("Recipes", systemImage: "book.closed") {
            NavigationStack {
                ScrollView {
                    VStack(spacing: 16) {
                        ForEach(0..<30, id: \.self) { index in
                            Text("Row \(index)")
                                .frame(maxWidth: .infinity, alignment: .leading)
                                .padding()
                        }
                    }
                }
                .navigationTitle("Spaghetti Carbonara")
            }
        }
        Tab("Meal Plan", systemImage: "calendar") {
            Text("Meal Plan")
        }
    }
    .tabBarMinimizeBehavior(.onScrollDown)
    .tabViewBottomAccessory {
        StartCookingAccessory {
        }
    }
}
