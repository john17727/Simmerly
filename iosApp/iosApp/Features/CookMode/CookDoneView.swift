import SwiftUI
import Shared

/// Mirrors recipes/presentation/cookmode/CookDoneView.kt (design frame 07). Tapping Done fires
/// `CookModeIntentFinishCooking`, which records last-made and a "Cooked" timeline event
/// (the note as its message) unconditionally, and the star rating only if the cook actually
/// tapped one — see the ViewModel for why.
struct CookDoneView: View {
    let state: CookModeState
    let onEvent: (CookModeIntent) -> Void
    let onExit: () -> Void

    private var stepCount: Int { state.steps.count }

    private var elapsedMinutes: Int64 {
        guard let started = state.cookingStartedAtMillis?.int64Value else { return 0 }
        let finished = state.cookingFinishedAtMillis?.int64Value ?? state.nowMillis
        return max((finished - started) / 60_000, 1)
    }

    var body: some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onExit) {
                    Image(systemName: "xmark")
                        .foregroundStyle(SimmerlyColor.onSurface)
                }
                Spacer()
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 8)

            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    RemoteImage(url: state.recipe.image, targetSize: CGSize(width: 400, height: 260)) {
                        Rectangle().fill(SimmerlyColor.surfaceContainer)
                    }
                    .frame(height: 260)
                    .clipShape(RoundedRectangle(cornerRadius: 12))

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Plated.")
                            .font(SimmerlyFont.headlineLarge)
                        Text(
                            "\(stepCount) \(stepCount == 1 ? "step" : "steps"), \(elapsedMinutes) minutes. "
                                + "Rate it while it is still in front of you."
                        )
                        .font(SimmerlyFont.bodyLarge)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }

                    HStack(spacing: 4) {
                        ForEach(1...5, id: \.self) { star in
                            Button(action: { onEvent(CookModeIntentSetRating(rating: Int32(star))) }) {
                                Image(systemName: star <= Int(state.rating) ? "star.fill" : "star")
                                    .font(.system(size: 28))
                                    .foregroundStyle(SimmerlyColor.primary)
                            }
                            .accessibilityLabel("Rate \(star) stars")
                        }
                    }

                    HStack(spacing: 8) {
                        Image(systemName: "bubble.left")
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        TextField(
                            "Leave a note for next time",
                            text: Binding(
                                get: { state.noteDraft },
                                set: { onEvent(CookModeIntentUpdateNote(text: $0)) }
                            )
                        )
                    }
                    .padding(12)
                    .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(SimmerlyColor.outline, lineWidth: 1))
                }
                .padding(.horizontal, 24)
            }

            VStack(spacing: 20) {
                Button {
                    onEvent(CookModeIntentFinishCooking.shared)
                    onExit()
                } label: {
                    Text("Done")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .controlSize(.large)

                Button("Back to the recipe", action: onExit)
                    .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 8)
        }
        .background(SimmerlyColor.background)
    }
}
