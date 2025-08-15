import UIKit
import SwiftUI
import ComposeApp

struct ComposeView: UIViewControllerRepresentable {
    let root: RootComponent
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(root: root)
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    let root: RootComponent
    var body: some View {
        ComposeView(root: root)
            .ignoresSafeArea()
    }
}



