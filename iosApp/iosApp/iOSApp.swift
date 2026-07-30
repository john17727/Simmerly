import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        doInitKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            RootView()
                .tint(SimmerlyColor.primary)
        }
    }
}
