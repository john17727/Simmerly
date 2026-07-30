import Foundation

/// Collects a single Kotlin `Flow`/`StateFlow` (bridged by SKIE as `AsyncSequence`) for the
/// lifetime of the calling task, forwarding each value to `onEach`. Intended to run inside a
/// `.task { }` view modifier so collection is cancelled automatically when the view disappears.
@MainActor
func observeFlow<Seq: AsyncSequence>(_ sequence: Seq, onEach: @escaping (Seq.Element) -> Void) async {
    do {
        for try await value in sequence {
            onEach(value)
        }
    } catch {
        // The underlying Kotlin Flow terminated (e.g. the ViewModel was disposed) — nothing to do.
    }
}

/// Collects a ViewModel's `stateFlow` and `sideEffectFlow` concurrently for the lifetime of the
/// calling task. Every screen's Root wires its own `onState`/`onSideEffect` closures and calls
/// this once from `.task { await observe(...) }`.
@MainActor
func observe<StateSeq: AsyncSequence, EffectSeq: AsyncSequence>(
    state: StateSeq,
    onState: @escaping (StateSeq.Element) -> Void,
    sideEffects: EffectSeq,
    onSideEffect: @escaping (EffectSeq.Element) -> Void
) async {
    await withTaskGroup(of: Void.self) { group in
        group.addTask { await observeFlow(state, onEach: onState) }
        group.addTask { await observeFlow(sideEffects, onEach: onSideEffect) }
    }
}
