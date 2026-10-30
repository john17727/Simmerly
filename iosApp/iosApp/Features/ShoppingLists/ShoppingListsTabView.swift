import SwiftUI
import Shared

/// Shopping List tab shell, replacing the Compose Navigation3 graph in
/// shoppinglists/presentation/ShoppingListsContent.kt. iPhone: NavigationStack push navigation.
/// iPad: NavigationSplitView list-detail, mirroring the ListDetailSceneStrategy behaviour.
struct ShoppingListsTabView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    /// A list to open, handed over from another tab. Cleared through `onRequestHandled` once shown.
    let request: String?
    let onRequestHandled: () -> Void

    var body: some View {
        if horizontalSizeClass == .regular {
            ShoppingListsSplitView(request: request, onRequestHandled: onRequestHandled)
        } else {
            ShoppingListsStackView(request: request, onRequestHandled: onRequestHandled)
        }
    }
}

/// Routes within the Shopping List tab. Mirrors shoppinglists/presentation/navigation/ShoppingListDestinations.kt.
enum ShoppingRoute: Hashable {
    case detail(listId: String)
}

private struct ShoppingListsStackView: View {
    let request: String?
    let onRequestHandled: () -> Void
    @State private var path: [ShoppingRoute] = []

    var body: some View {
        NavigationStack(path: $path) {
            ShoppingListsRoot(onListSelected: { path.append(.detail(listId: $0)) })
                .navigationDestination(for: ShoppingRoute.self) { route in
                    switch route {
                    case .detail(let listId):
                        ShoppingListDetailRoot(
                            listId: listId,
                            onNavigateBack: { path.removeLast() }
                        )
                    }
                }
        }
        // Replace whatever is pushed rather than stacking on it, like ShoppingListsContent.kt.
        // `initial` covers the tab being created by the very selection change that sent the request.
        .onChange(of: request, initial: true) { _, listId in
            guard let listId else { return }
            path = [.detail(listId: listId)]
            onRequestHandled()
        }
    }
}

private struct ShoppingListsSplitView: View {
    let request: String?
    let onRequestHandled: () -> Void
    @State private var selectedListId: String?

    var body: some View {
        NavigationSplitView {
            ShoppingListsRoot(onListSelected: { selectedListId = $0 })
        } detail: {
            if let listId = selectedListId {
                NavigationStack {
                    ShoppingListDetailRoot(
                        listId: listId,
                        onNavigateBack: { selectedListId = nil }
                    )
                    .id(listId)
                }
            } else {
                ContentUnavailableView(
                    "Select a list to see its items",
                    systemImage: "cart"
                )
            }
        }
        .onChange(of: request, initial: true) { _, listId in
            guard let listId else { return }
            selectedListId = listId
            onRequestHandled()
        }
    }
}
