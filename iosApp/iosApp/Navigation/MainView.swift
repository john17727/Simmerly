import SwiftUI
import Shared

@MainActor
@Observable
final class ProfileTabModel {
    private let vm: ProfileViewModel
    private(set) var user: UiUser?

    init() {
        vm = SimmerlyViewModels.shared.profile()
        user = vm.user.value
    }

    deinit {
        SimmerlyViewModels.shared.dispose(viewModel: vm)
    }

    func activate() async {
        await observeFlow(vm.user) { [weak self] newUser in self?.user = newUser }
    }
}

/// Tab shell, mirroring navigation/app/AppContent.kt. Recipes and Shopping List have real content;
/// Meal Plan and Profile are stubs on the Compose side too.
struct MainView: View {
    @State private var model = ProfileTabModel()
    @State private var accessory = TabAccessoryModel()
    @State private var selectedTab: AppTab = .recipes

    var body: some View {
        TabView(selection: $selectedTab) {
            ForEach(AppTab.allCases, id: \.self) { tab in
                Tab(tab.title, systemImage: tab.systemImage, value: tab) {
                    switch tab {
                    case .recipes:
                        RecipesTabView()
                    case .shoppingList:
                        ShoppingListsTabView()
                    case .mealPlan:
                        Text(tab.title)
                    case .profile:
                        Text(model.user?.name ?? tab.title)
                    }
                }
            }
        }
        .environment(accessory)
        // Collapses the bar into a capsule on scroll so the accessory below can take over the row.
        .tabBarMinimizeBehavior(.onScrollDown)
        // Declared here because `tabViewBottomAccessory` only takes effect on the `TabView` itself;
        // the owning screen publishes through TabAccessoryModel. `isEnabled` is what hides the
        // accessory — an empty body still leaves the container's glass capsule behind — and it
        // keeps the modifier attached, so the accessory animates in and out instead of popping.
        .tabViewBottomAccessory(isEnabled: accessoryContent != nil) {
            switch accessoryContent {
            case .startCooking(let action):
                StartCookingAccessory(action: action)
            case .shoppingLists:
                ShoppingListsAccessory(model: accessory)
            case .addItem:
                AddItemAccessory(model: accessory)
            case nil:
                EmptyView()
            }
        }
        .task { await model.activate() }
    }

    /// Tabs keep their state alive while backgrounded, so gate on the selection here rather than
    /// expecting them to clear what they published.
    private var accessoryContent: AccessoryContent? {
        switch selectedTab {
        case .recipes:
            return accessory.startCooking.map { .startCooking($0) }
        case .shoppingList:
            // A list's add-item bar wins over the overview's while the list is pushed on top.
            if accessory.addItem != nil { return .addItem }
            if accessory.shoppingLists != nil { return .shoppingLists }
            return nil
        case .mealPlan, .profile:
            return nil
        }
    }
}

private enum AccessoryContent {
    case startCooking(() -> Void)
    case shoppingLists
    case addItem
}
