import SwiftUI
import Shared
import UserNotifications

/// Mirrors recipes/presentation/cookmode/CookModeScreen.kt: the entry point for the full-screen
/// cooking flow, hosting whichever phase view is current plus the two timer sheets. Presented as
/// a `.fullScreenCover` from `RecipesTabView` — see that file for why it's an overlay rather than
/// a pushed destination, matching Compose's `BottomSheetSceneStrategy`.
@MainActor
@Observable
final class CookModeModel {
    private let vm: CookModeViewModel
    private(set) var state: CookModeState
    /// Ticks at ~4Hz whenever a timer is running (see `CookModeViewModel.runTicker`). Split out
    /// from `state` so a timer countdown can read it without invalidating every `@Observable`
    /// consumer of `model.state` four times a second — see `apply(_:)`.
    private(set) var nowMillis: Int64
    private(set) var didExit = false

    init(recipeId: String) {
        vm = SimmerlyViewModels.shared.cookMode(recipeId: recipeId)
        let initial = vm.stateFlow.value
        state = initial
        nowMillis = initial.nowMillis
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        await observe(
            state: vm.stateFlow,
            onState: { [weak self] newState in self?.apply(newState) },
            sideEffects: vm.sideEffectFlow,
            onSideEffect: { [weak self] effect in
                switch onEnum(of: effect) {
                case .exit:
                    self?.didExit = true
                case .timerFinished:
                    // The Compose side deliberately ignores this side effect — the rail/sheet
                    // already reflect it live. iOS adds a haptic here since backgrounded alerting
                    // is handled separately by IosCookTimerAlerts (a local notification).
                    UINotificationFeedbackGenerator().notificationOccurred(.success)
                }
            }
        )
    }

    func send(_ intent: CookModeIntent) {
        vm.onEvent(event: intent)
    }

    /// Always advances the clock, but only republishes `state` when something other than the
    /// clock actually changed — see `CookModeState.withoutNow()`.
    private func apply(_ newState: CookModeState) {
        nowMillis = newState.nowMillis
        if newState.withoutNow() != state.withoutNow() {
            state = newState
        }
    }
}

struct CookModeRoot: View {
    @State private var model: CookModeModel
    let onExit: () -> Void

    init(recipeId: String, onExit: @escaping () -> Void) {
        _model = State(initialValue: CookModeModel(recipeId: recipeId))
        self.onExit = onExit
    }

    var body: some View {
        Group {
            if model.state.loading {
                ProgressView()
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                    .background(SimmerlyColor.background)
            } else {
                phaseContent
                    .animation(.easeInOut(duration: 0.2), value: model.state.phase)
            }
        }
        .keepsScreenAwake()
        .task { await model.activate() }
        .task {
            // Fire-and-forget: a cook who declines still gets the in-app rail and the foreground
            // haptic above, just not a notification while backgrounded.
            _ = try? await UNUserNotificationCenter.current()
                .requestAuthorization(options: [.alert, .badge, .sound])
        }
        .onChange(of: model.didExit) { _, didExit in
            if didExit { onExit() }
        }
        .sheet(
            isPresented: Binding(
                get: { model.state.newTimerDraft != nil },
                set: { isPresented in
                    if !isPresented { model.send(CookModeIntentDismissNewTimerSheet.shared) }
                }
            )
        ) {
            if let draft = model.state.newTimerDraft {
                NewTimerSheet(
                    draft: draft,
                    presets: TimerPresetKt.toTimerPresets(model.state.steps),
                    onDraftChange: { model.send(CookModeIntentUpdateTimerDraft(draft: $0)) },
                    onDismiss: { model.send(CookModeIntentDismissNewTimerSheet.shared) },
                    onConfirm: { model.send(CookModeIntentConfirmNewTimer.shared) }
                )
                .interactiveDismissDisabled(true)
                .presentationDetents([.large])
            }
        }
        .sheet(
            isPresented: Binding(
                get: { model.state.showTimerList },
                set: { isPresented in
                    if !isPresented { model.send(CookModeIntentDismissTimerList.shared) }
                }
            )
        ) {
            TimerListSheet(
                timers: model.state.timers,
                nowMillis: model.nowMillis,
                onDismiss: { model.send(CookModeIntentDismissTimerList.shared) },
                onAddTimer: { model.send(CookModeIntentShowNewTimerSheet.shared) },
                onPauseTimer: { model.send(CookModeIntentPauseTimer(id: $0)) },
                onResumeTimer: { model.send(CookModeIntentResumeTimer(id: $0)) },
                onCancelTimer: { model.send(CookModeIntentCancelTimer(id: $0)) }
            )
            .presentationDetents([.medium, .large])
        }
    }

    @ViewBuilder
    private var phaseContent: some View {
        switch model.state.phase {
        case .miseEnPlace:
            MiseEnPlaceView(state: model.state, onEvent: model.send, onExit: onExit)
        case .steps:
            CookStepView(state: model.state, nowMillis: model.nowMillis, onEvent: model.send, onExit: onExit)
        case .done:
            CookDoneView(state: model.state, onEvent: model.send, onExit: onExit)
        }
    }
}
