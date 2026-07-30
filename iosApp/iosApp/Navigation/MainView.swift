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
    @State private var selectedTab: AppTab = .recipes

    var body: some View {
        TabView(selection: $selectedTab) {
            ForEach(AppTab.allCases, id: \.self) { tab in
                Group {
                    switch tab {
                    case .recipes:
                        RecipesTabView()
                    case .mealPlan, .shoppingList:
                        Text(tab.title)
                    case .profile:
                        Text(model.user?.name ?? tab.title)
                    }
                }
                .tabItem { Label(tab.title, systemImage: tab.systemImage) }
                .tag(tab)
            }
        }
        .task { await model.activate() }
    }
}
