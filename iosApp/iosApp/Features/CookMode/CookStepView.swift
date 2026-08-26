import SwiftUI
import Shared

private let maxProgressSegments = 10

/// Mirrors recipes/presentation/cookmode/CookStepView.kt (design frames 03 and 05 — the same view,
/// the only difference is whether `state.timers` is empty). Renders whichever of image /
/// ingredient chips / detected-duration card the step actually has; a bare text-only step degrades
/// cleanly.
struct CookStepView: View {
    let state: CookModeState
    let nowMillis: Int64
    let onEvent: (CookModeIntent) -> Void
    let onExit: () -> Void

    private var steps: [CookStepUi] { state.steps }

    var body: some View {
        if let step = state.currentStep {
            content(step: step)
        }
    }

    @ViewBuilder
    private func content(step: CookStepUi) -> some View {
        let isLastStep = Int(state.stepIndex) == steps.count - 1
        let nextStepName = steps.count > Int(state.stepIndex) + 1
            ? steps[Int(state.stepIndex) + 1].instruction.summary
            : nil

        VStack(spacing: 0) {
            header(step: step)
            ScrollView {
                StepBody(
                    step: step,
                    timers: state.timers,
                    timerOptionsMillis: state.currentStepTimerOptionsMillis.map { $0.int64Value },
                    selectedOptionMillis: state.selectedRangeOptionMillis?.int64Value,
                    onStartDetectedTimer: { duration in
                        onEvent(CookModeIntentStartDetectedTimer(duration: duration, label: step.instruction.summary))
                    },
                    onSelectRangeOption: { millis in
                        onEvent(CookModeIntentKt.selectRangeOptionMillis(millis: millis))
                    },
                    onStartSelectedRangeTimer: {
                        onEvent(CookModeIntentStartSelectedRangeTimer(label: step.instruction.summary))
                    }
                )
                .padding(16)
            }
            bottomBar(isLastStep: isLastStep, nextStepName: nextStepName)
        }
        .background(SimmerlyColor.background)
    }

    private func header(step: CookStepUi) -> some View {
        VStack(spacing: 0) {
            HStack {
                Button(action: onExit) {
                    Image(systemName: "xmark")
                        .foregroundStyle(SimmerlyColor.onSurface)
                }
                VStack(alignment: .leading, spacing: 0) {
                    Text("STEP \(state.stepIndex + 1) OF \(steps.count)")
                        .font(SimmerlyFont.labelSmall)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    Text(state.recipe.title.localized)
                        .font(SimmerlyFont.titleSmall)
                        .lineLimit(1)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            }
            .padding(.horizontal, 16)
            .frame(height: 64)

            StepProgress(current: Int(state.stepIndex), total: steps.count)
                .padding(.horizontal, 16)

            TimerRail(
                timers: state.timers,
                nowMillis: nowMillis,
                onTimerClick: { _ in onEvent(CookModeIntentShowTimerList.shared) },
                onAddTimerClick: { onEvent(CookModeIntentShowNewTimerSheet.shared) }
            )
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 4)
        }
    }

    private func bottomBar(isLastStep: Bool, nextStepName: String?) -> some View {
        HStack(spacing: 12) {
            Button(action: { onEvent(CookModeIntentPreviousStep.shared) }) {
                Image(systemName: "arrow.left")
            }
            .buttonStyle(.bordered)
            .controlSize(.large)
            .frame(width: 48)

            Button {
                onEvent(CookModeIntentNextStep.shared)
            } label: {
                HStack(spacing: 8) {
                    Text(isLastStep ? "Finish" : nextStepName.map { "Next: \($0)" } ?? "Next step")
                        .lineLimit(1)
                    Image(systemName: isLastStep ? "checkmark" : "arrow.right")
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .controlSize(.large)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 8)
    }
}

private struct StepProgress: View {
    let current: Int
    let total: Int

    var body: some View {
        if total <= maxProgressSegments {
            HStack(spacing: 4) {
                ForEach(0..<total, id: \.self) { index in
                    Capsule()
                        .fill(index <= current ? SimmerlyColor.primary : SimmerlyColor.outlineVariant)
                        .frame(height: 4)
                }
            }
        } else {
            ProgressView(value: Double(current + 1), total: Double(total))
                .tint(SimmerlyColor.primary)
        }
    }
}

private struct StepBody: View {
    let step: CookStepUi
    let timers: [CookTimerUi]
    let timerOptionsMillis: [Int64]
    let selectedOptionMillis: Int64?
    let onStartDetectedTimer: (ParsedDuration) -> Void
    let onSelectRangeOption: (Int64) -> Void
    let onStartSelectedRangeTimer: () -> Void

    private var firstDetected: ParsedDuration? { step.detectedDurations.first }
    private var alreadyStarted: Bool {
        timers.contains { $0.origin == .detected && $0.label == step.instruction.summary }
    }

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            if let image = step.instruction.images.first {
                RemoteImage(url: image) {
                    Rectangle().fill(SimmerlyColor.surfaceContainer)
                }
                .aspectRatio(contentMode: .fit)
                .frame(maxWidth: .infinity)
                .clipShape(RoundedRectangle(cornerRadius: 12))
            }

            Text(step.instruction.summary)
                .font(SimmerlyFont.headlineMedium)
                .foregroundStyle(SimmerlyColor.onSurface)

            if !step.ingredients.isEmpty {
                FlowLayout(spacing: 8) {
                    ForEach(step.ingredients, id: \.referenceId) { ingredient in
                        StepIngredientChip(ingredient: ingredient)
                    }
                }
            }

            MarkdownText(markdown: step.instruction.text)
                .font(SimmerlyFont.bodyLarge)
                .foregroundStyle(SimmerlyColor.onSurface)

            if let firstDetected, !alreadyStarted {
                if firstDetected.isRange {
                    DetectedRangeCard(
                        optionsMillis: timerOptionsMillis,
                        selectedMillis: selectedOptionMillis,
                        onSelect: onSelectRangeOption,
                        onStart: onStartSelectedRangeTimer
                    )
                } else {
                    DetectedTimerCard(durationMillis: firstDetected.durationMillis) {
                        onStartDetectedTimer(firstDetected)
                    }
                }
            }
        }
    }
}

private struct StepIngredientChip: View {
    let ingredient: IngredientUi

    var body: some View {
        HStack(spacing: 4) {
            if let quantity = ingredient.formattedQuantity {
                Text(quantity).fontWeight(.bold)
            }
            Text(ingredient.formattedDisplay)
        }
        .font(SimmerlyFont.bodySmall)
        .foregroundStyle(SimmerlyColor.onTertiaryContainer)
        .padding(.vertical, 4)
        .padding(.horizontal, 8)
        .background(SimmerlyColor.tertiaryContainer, in: RoundedRectangle(cornerRadius: 8))
    }
}

/// The detected-range variant of the timer card: a step that says "cook for 15-17 minutes" can't
/// be reduced to one number, so every reasonable duration in the range is offered as a chip with
/// the shortest preselected.
private struct DetectedRangeCard: View {
    let optionsMillis: [Int64]
    let selectedMillis: Int64?
    let onSelect: (Int64) -> Void
    let onStart: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Image(systemName: "timer")
                    .foregroundStyle(SimmerlyColor.secondary)
                Text("Range detected · \(optionsMillis.count) timers suggested")
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }

            FlowLayout(spacing: 8) {
                ForEach(optionsMillis, id: \.self) { option in
                    let isSelected = option == selectedMillis
                    Text(formatPresetLength(millis: option))
                        .font(SimmerlyFont.titleSmall)
                        .foregroundStyle(isSelected ? SimmerlyColor.onPrimary : SimmerlyColor.onSurface)
                        .padding(.horizontal, 16)
                        .padding(.vertical, 10)
                        .background(
                            isSelected ? SimmerlyColor.primary : SimmerlyColor.surface,
                            in: RoundedRectangle(cornerRadius: 8)
                        )
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .strokeBorder(isSelected ? SimmerlyColor.primary : SimmerlyColor.outlineVariant, lineWidth: 1)
                        )
                        .onTapGesture { onSelect(option) }
                }
            }

            Button {
                onStart()
            } label: {
                HStack(spacing: 8) {
                    Image(systemName: "play.fill")
                    Text(selectedMillis.map { "Start \(formatTimerLength(millis: $0)) timer" } ?? "Start timer")
                }
                .frame(maxWidth: .infinity)
            }
            .buttonStyle(.borderedProminent)
            .disabled(selectedMillis == nil)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 14)
        .background(SimmerlyColor.primary.opacity(0.08), in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.primaryContainer, lineWidth: 1))
    }
}

private struct DetectedTimerCard: View {
    let durationMillis: Int64
    let onStart: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: "timer")
                .font(.system(size: 20))
                .foregroundStyle(SimmerlyColor.secondary)
            VStack(alignment: .leading, spacing: 0) {
                Text("\(formatPresetLength(millis: durationMillis)) timer")
                    .font(SimmerlyFont.titleMedium)
                Text("Detected in this step")
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }
            Spacer()
            Button("Start", action: onStart)
                .buttonStyle(.borderedProminent)
        }
        .padding(12)
        .background(SimmerlyColor.primary.opacity(0.08), in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.primaryContainer, lineWidth: 1))
    }
}
