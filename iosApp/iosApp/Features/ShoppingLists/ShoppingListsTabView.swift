import SwiftUI
import Shared

/// Shopping List tab shell, replacing the Compose Navigation3 graph in
/// shoppinglists/presentation/ShoppingListsContent.kt. iPhone: NavigationStack push navigation.
/// iPad: NavigationSplitView list-detail, mirroring the ListDetailSceneStrategy behaviour.
struct ShoppingListsTabView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass

    var body: some View {
        if horizontalSizeClass == .regular {
            ShoppingListsSplitView()
        } else {
            ShoppingListsStackView()
        }
    }
}

/// Routes within the Shopping List tab. Mirrors shoppinglists/presentation/navigation/ShoppingListDestinations.kt.
enum ShoppingRoute: Hashable {
    case detail(listId: String)
}

private struct ShoppingListsStackView: View {
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
    }
}

private struct ShoppingListsSplitView: View {
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
    }
}
