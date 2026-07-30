package dev.juanrincon.simmerly.di

private var koinStarted = false

/**
 * Starts Koin once for the process. Safe to call multiple times — e.g. from SwiftUI previews or
 * if the app entry point runs more than once — unlike [initKoin], which crashes on a second call.
 */
fun initKoinIos() {
    if (koinStarted) return
    koinStarted = true
    initKoin()
}
