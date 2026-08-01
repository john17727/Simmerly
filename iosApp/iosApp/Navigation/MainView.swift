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

/// Tab shell, mirroring navigation/app/AppContent.kt. Only the Recipes tab has real content today —
/// Meal Plan, Shopping List, and Profile are stubs on the Compose side too.
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
                    case .mealPlan, .shoppingList:
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
        .tabViewBottomAccessory(isEnabled: startCooking != nil) {
            if let startCooking {
                StartCookingAccessory(action: startCooking)
            }
        }
        .task { await model.activate() }
    }

    /// Only the Recipes tab publishes an accessory today, and the other tabs keep their state alive
    /// while backgrounded — so gate on the selection here rather than expecting them to clear it.
    private var startCooking: (() -> Void)? {
        selectedTab == .recipes ? accessory.startCooking : nil
    }
}
