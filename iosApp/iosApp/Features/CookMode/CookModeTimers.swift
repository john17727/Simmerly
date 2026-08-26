import SwiftUI
import Shared

/// Ports of recipes/presentation/cookmode/CookModeTimers.kt: the timer rail pinned above every
/// step, the timer-list sheet, and the new-timer sheet. `CookTimerUi`'s `total`/`remaining` are
/// `kotlin.time.Duration`, which bridges to Swift as an opaque raw value rather than a millisecond
/// count — every call site here goes through the `…Millis` accessors added in commonMain instead.

// MARK: - Rail

/// The row of running-timer chips pinned above every step, plus the "add timer" affordance. Shows
/// a "No timers running" line instead of an empty rail.
struct TimerRail: View {
    let timers: [CookTimerUi]
    let nowMillis: Int64
    let onTimerClick: (String) -> Void
    let onAddTimerClick: () -> Void

    var body: some View {
        HStack(spacing: 8) {
            if timers.isEmpty {
                HStack(spacing: 8) {
                    Image(systemName: "timer")
                        .font(.system(size: 14))
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    Text("No timers running")
                        .font(SimmerlyFont.bodySmall)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
            } else {
                ScrollView(.horizontal, showsIndicators: false) {
                    HStack(spacing: 8) {
                        ForEach(timers, id: \.id) { timer in
                            TimerChip(timer: timer, nowMillis: nowMillis, onClick: { onTimerClick(timer.id) })
                        }
                    }
                }
            }
            AddTimerButton(action: onAddTimerClick)
        }
    }
}

private struct AddTimerButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "plus")
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                .frame(width: 40, height: 40)
                .overlay(
                    RoundedRectangle(cornerRadius: 8)
                        .strokeBorder(SimmerlyColor.outline, style: StrokeStyle(lineWidth: 1, dash: [4, 4]))
                )
        }
        .accessibilityLabel("Add timer")
    }
}

private struct TimerChip: View {
    let timer: CookTimerUi
    let nowMillis: Int64
    let onClick: () -> Void

    private var isDetected: Bool { timer.origin == .detected }
    private var ringColor: Color { isDetected ? SimmerlyColor.primary : SimmerlyColor.tertiary }
    private var borderColor: Color { isDetected ? SimmerlyColor.primaryContainer : SimmerlyColor.outlineVariant }
    private var containerColor: Color {
        isDetected ? SimmerlyColor.primary.opacity(0.08) : SimmerlyColor.surfaceContainer
    }

    var body: some View {
        Button(action: onClick) {
            HStack(spacing: 8) {
                TimerRing(progress: timer.progress(nowMillis: nowMillis), color: ringColor, trackColor: borderColor)
                    .frame(width: 24, height: 24)
                VStack(alignment: .leading, spacing: 0) {
                    Text(formatTimer(millis: timer.remainingMillis(nowMillis: nowMillis)))
                        .font(SimmerlyFont.labelLarge)
                        .foregroundStyle(SimmerlyColor.onSurface)
                    Text(timer.label)
                        .font(SimmerlyFont.labelSmall)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        .lineLimit(1)
                }
            }
            .padding(.leading, 10)
            .padding(.trailing, 12)
            .padding(.vertical, 8)
            .background(containerColor, in: RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(borderColor, lineWidth: 1))
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Timer list sheet

struct TimerListSheet: View {
    let timers: [CookTimerUi]
    let nowMillis: Int64
    let onDismiss: () -> Void
    let onAddTimer: () -> Void
    let onPauseTimer: (String) -> Void
    let onResumeTimer: (String) -> Void
    let onCancelTimer: (String) -> Void

    private var ordered: [CookTimerUi] {
        timers.sorted { a, b in
            let aFinished = a.isFinished(nowMillis: nowMillis)
            let bFinished = b.isFinished(nowMillis: nowMillis)
            if aFinished != bFinished { return aFinished && !bFinished }
            if a.isPaused != b.isPaused { return !a.isPaused && b.isPaused }
            return a.remainingMillis(nowMillis: nowMillis) < b.remainingMillis(nowMillis: nowMillis)
        }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    if timers.isEmpty {
                        Text("No timers yet.")
                            .font(SimmerlyFont.bodyMedium)
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                            .padding(.vertical, 16)
                    } else {
                        ForEach(ordered, id: \.id) { timer in
                            TimerListEntry(
                                timer: timer,
                                nowMillis: nowMillis,
                                onPause: { onPauseTimer(timer.id) },
                                onResume: { onResumeTimer(timer.id) },
                                onCancel: { onCancelTimer(timer.id) }
                            )
                        }
                    }
                    HStack(spacing: 8) {
                        Image(systemName: "bell.badge")
                            .font(.system(size: 14))
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        Text("Timers keep running if you leave cook mode.")
                            .font(SimmerlyFont.bodySmall)
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }
                    .padding(.top, 8)
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 24)
            }
            .navigationTitle("Timers")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Add timer", action: onAddTimer)
                }
            }
        }
    }
}

private struct TimerListEntry: View {
    let timer: CookTimerUi
    let nowMillis: Int64
    let onPause: () -> Void
    let onResume: () -> Void
    let onCancel: () -> Void

    private var finished: Bool { timer.isFinished(nowMillis: nowMillis) }
    private var ringColor: Color { timer.origin == .detected ? SimmerlyColor.primary : SimmerlyColor.tertiary }
    // The design system has no inverse-surface role, unlike Compose's Material 3 scheme — primary/
    // onPrimary stands in for the finished row's high-contrast flip.
    private var containerColor: Color {
        if finished { return SimmerlyColor.primary }
        if timer.origin == .detected && !timer.isPaused { return SimmerlyColor.primary.opacity(0.08) }
        return SimmerlyColor.surfaceContainer
    }
    private var contentColor: Color { finished ? SimmerlyColor.onPrimary : SimmerlyColor.onSurface }
    private var subtitleColor: Color {
        finished ? SimmerlyColor.onPrimary.opacity(0.8) : SimmerlyColor.onSurfaceVariant
    }
    private var subtitle: String {
        if finished { return "\(timer.label) · finished" }
        if timer.isPaused { return "\(timer.label) · paused" }
        return "\(timer.label) · \(formatPresetLength(millis: timer.totalMillis))"
    }

    var body: some View {
        HStack(spacing: 16) {
            if finished {
                Image(systemName: "bell.badge.fill")
                    .font(.system(size: 22))
                    .foregroundStyle(SimmerlyColor.onPrimary)
                    .frame(width: 28, height: 28)
            } else {
                ZStack {
                    TimerRing(
                        progress: timer.progress(nowMillis: nowMillis),
                        color: ringColor,
                        trackColor: SimmerlyColor.outlineVariant,
                        lineWidth: 3
                    )
                    Image(systemName: timer.isPaused ? "pause.fill" : "timer")
                        .font(.system(size: 14))
                        .foregroundStyle(ringColor)
                }
                .frame(width: 44, height: 44)
            }
            VStack(alignment: .leading, spacing: 2) {
                Text(formatTimer(millis: timer.remainingMillis(nowMillis: nowMillis)))
                    .font(SimmerlyFont.headlineSmall)
                    .foregroundStyle(contentColor)
                Text(subtitle)
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(subtitleColor)
            }
            Spacer()
            if finished {
                Button("Stop", action: onCancel)
                    .foregroundStyle(SimmerlyColor.primary)
            } else {
                HStack(spacing: 4) {
                    Button(action: timer.isPaused ? onResume : onPause) {
                        Image(systemName: timer.isPaused ? "play.fill" : "pause.fill")
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }
                    Button(action: onCancel) {
                        Image(systemName: "xmark")
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }
                }
            }
        }
        .padding(16)
        .background(containerColor, in: RoundedRectangle(cornerRadius: 12))
    }
}

// MARK: - New timer sheet

struct NewTimerSheet: View {
    let draft: NewTimerDraft
    let presets: [TimerPreset]
    let onDraftChange: (NewTimerDraft) -> Void
    let onDismiss: () -> Void
    let onConfirm: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("New timer")
                .font(SimmerlyFont.headlineSmall)
                .padding(.top, 16)

            DurationWheelRow(
                minutes: Binding(
                    get: { Int(draft.minutes) },
                    set: { onDraftChange(draft.doCopy(minutes: Int32($0), seconds: draft.seconds, label: draft.label)) }
                ),
                seconds: Binding(
                    get: { Int(draft.seconds) },
                    set: { onDraftChange(draft.doCopy(minutes: draft.minutes, seconds: Int32($0), label: draft.label)) }
                )
            )
            .frame(height: 180)

            TextField(
                "Name",
                text: Binding(
                    get: { draft.label },
                    set: { onDraftChange(draft.doCopy(minutes: draft.minutes, seconds: draft.seconds, label: $0)) }
                )
            )
            .textFieldStyle(.roundedBorder)

            if !presets.isEmpty {
                Text("FROM THIS RECIPE")
                    .font(SimmerlyFont.labelSmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)

                PresetChipRow(presets: presets) { preset in
                    let totalSeconds = preset.durationMillis / 1000
                    onDraftChange(
                        draft.doCopy(
                            minutes: Int32(totalSeconds / 60),
                            seconds: Int32(totalSeconds % 60),
                            label: draft.label
                        )
                    )
                }
            }

            HStack(spacing: 12) {
                Button("Cancel", action: onDismiss)
                    .buttonStyle(.bordered)
                    .frame(maxWidth: .infinity)

                Button {
                    onConfirm()
                } label: {
                    Label("Start", systemImage: "play.fill")
                        .frame(maxWidth: .infinity)
                }
                .buttonStyle(.borderedProminent)
                .disabled(draft.minutes == 0 && draft.seconds == 0)
            }
            .controlSize(.large)
            .padding(.bottom, 8)

            Spacer(minLength: 0)
        }
        .padding(.horizontal, 24)
    }
}

private struct PresetChipRow: View {
    let presets: [TimerPreset]
    let onSelect: (TimerPreset) -> Void

    var body: some View {
        FlowLayout(spacing: 8) {
            ForEach(Array(presets.enumerated()), id: \.offset) { _, preset in
                Button(action: { onSelect(preset) }) {
                    Text(preset.label)
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurface)
                        .padding(.horizontal, 14)
                        .padding(.vertical, 8)
                        .background(SimmerlyColor.surfaceContainer, in: RoundedRectangle(cornerRadius: 8))
                        .overlay(RoundedRectangle(cornerRadius: 8).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
                }
                .buttonStyle(.plain)
            }
        }
    }
}

/// Native `Picker(.wheel)` replaces Compose's 2000-item looping `LazyColumn` — iOS's own wheel
/// picker already handles the "spin past a boundary" case Compose had to build by hand.
private struct DurationWheelRow: View {
    @Binding var minutes: Int
    @Binding var seconds: Int

    var body: some View {
        HStack(spacing: 4) {
            Picker("Minutes", selection: $minutes) {
                ForEach(0..<60) { value in
                    Text(String(format: "%02d", value)).tag(value)
                }
            }
            .pickerStyle(.wheel)
            .frame(maxWidth: .infinity)

            Text("min")
                .font(SimmerlyFont.labelSmall)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)

            Picker("Seconds", selection: $seconds) {
                ForEach(0..<60) { value in
                    Text(String(format: "%02d", value)).tag(value)
                }
            }
            .pickerStyle(.wheel)
            .frame(maxWidth: .infinity)

            Text("sec")
                .font(SimmerlyFont.labelSmall)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
        }
    }
}

// MARK: - Shared helpers

private struct TimerRing: View {
    let progress: Float
    let color: Color
    let trackColor: Color
    var lineWidth: CGFloat = 3

    var body: some View {
        ZStack {
            Circle().stroke(trackColor, style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
            Circle()
                .trim(from: 0, to: CGFloat(min(max(progress, 0), 1)))
                .stroke(color, style: StrokeStyle(lineWidth: lineWidth, lineCap: .round))
                .rotationEffect(.degrees(-90))
        }
    }
}

/// "3:07" — a running countdown display.
func formatTimer(millis: Int64) -> String {
    let totalSeconds = max(millis / 1000, 0)
    let minutes = totalSeconds / 60
    let seconds = totalSeconds % 60
    return "\(minutes):\(String(format: "%02d", seconds))"
}

/// "8 min" / "45 sec" — a coarse, human duration label. Used for the timer-list subtitle here and
/// for the detected-duration card's headline in `CookStepView.swift`.
func formatPresetLength(millis: Int64) -> String {
    let minutes = millis / 60_000
    if minutes > 0 { return "\(minutes) min" }
    return "\(millis / 1000) sec"
}

/// "15 minute" / "45 second" — the spelled-out form, for prose like "Start 15 minute timer" where
/// the abbreviated `formatPresetLength` would read as clipped.
func formatTimerLength(millis: Int64) -> String {
    let minutes = millis / 60_000
    if minutes > 0 { return "\(minutes) minute" }
    return "\(millis / 1000) second"
}
