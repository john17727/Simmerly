import SwiftUI
import Shared

/// Mirrors shoppinglists/presentation/list/ShoppingListsScreen.kt: the large "Shopping lists"
/// title (which collapses into the bar on scroll natively), Mine / Household chips and list cards.
/// Search and "New list" live in the tab view's bottom accessory (`ShoppingListsAccessory`).
struct ShoppingListsView: View {
    let state: ShoppingListsState
    let onEvent: (ShoppingListsIntent) -> Void

    /// The name being typed into the create or rename alert.
    @State private var nameDraft = ""

    var body: some View {
        content
            .navigationTitle("Shopping lists")
            .alert(dialogTitle, isPresented: nameDialogPresented) {
                TextField("List name", text: $nameDraft)
                    .textInputAutocapitalization(.sentences)
                Button(dialogConfirmLabel) { confirmNameDialog() }
                    .disabled(nameDraft.trimmingCharacters(in: .whitespaces).isEmpty)
                Button("Cancel", role: .cancel) { onEvent(ShoppingListsIntentOnDialogDismissed.shared) }
            }
            // Seeds the name field each time a create or rename dialog opens.
            .onChange(of: nameDialogSeed) { _, seed in nameDraft = seed ?? "" }
            .alert(deleteTitle, isPresented: deleteDialogPresented) {
                Button("Delete", role: .destructive) { onEvent(ShoppingListsIntentOnDeleteConfirmed.shared) }
                Button("Cancel", role: .cancel) { onEvent(ShoppingListsIntentOnDialogDismissed.shared) }
            } message: {
                Text("The list and all of its items will be removed for everyone in your household.")
            }
    }

    @ViewBuilder
    private var content: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                filterChips
                if state.allLists == nil, let error = state.error {
                    ContentUnavailableView {
                        Label(error.localized, systemImage: "cart")
                    } actions: {
                        Button("Try again") { onEvent(ShoppingListsIntentOnRefresh.shared) }
                    }
                    .padding(.top, 48)
                } else if state.allLists == nil {
                    VStack(spacing: 8) {
                        ForEach(0..<3, id: \.self) { _ in ShoppingListCardSkeleton() }
                    }
                } else if state.lists.isEmpty {
                    ContentUnavailableView(emptyTitle, systemImage: "cart", description: emptyDescription)
                        .padding(.top, 48)
                } else {
                    LazyVStack(spacing: 8) {
                        ForEach(state.lists, id: \.id) { list in
                            ShoppingListCard(
                                list: list,
                                onTap: { onEvent(ShoppingListsIntentOnListClicked(listId: list.id)) },
                                onRename: { onEvent(ShoppingListsIntentOnRenameClicked(listId: list.id)) },
                                onDelete: { onEvent(ShoppingListsIntentOnDeleteClicked(listId: list.id)) }
                            )
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 16)
        }
        .refreshable { onEvent(ShoppingListsIntentOnRefresh.shared) }
    }

    private var filterChips: some View {
        HStack(spacing: 8) {
            ForEach([ListFilter.mine, ListFilter.household], id: \.self) { filter in
                FilterChipButton(
                    title: filter == .mine ? "Mine" : "Household",
                    isSelected: state.filter == filter,
                    action: { onEvent(ShoppingListsIntentOnFilterSelected(filter: filter)) }
                )
            }
        }
        .padding(.top, 4)
    }

    // MARK: - Empty state

    private var emptyTitle: String {
        if !state.query.trimmingCharacters(in: .whitespaces).isEmpty {
            return "No lists match “\(state.query.trimmingCharacters(in: .whitespaces))”"
        }
        return state.filter == .mine ? "You have no shopping lists yet" : "Your household has no shopping lists yet"
    }

    private var emptyDescription: Text? {
        state.query.isEmpty ? Text("Start one with New list, then add items by hand.") : nil
    }

    // MARK: - Dialogs

    private var dialogTitle: String {
        switch onEnum(of: state.dialog) {
        case .rename: return "Rename list"
        default: return "New list"
        }
    }

    private var dialogConfirmLabel: String {
        switch onEnum(of: state.dialog) {
        case .rename: return "Save"
        default: return "Create"
        }
    }

    /// The name field's starting text while a create or rename dialog is open, `nil` otherwise.
    private var nameDialogSeed: String? {
        switch onEnum(of: state.dialog) {
        case .create: return ""
        case .rename(let rename): return rename.currentName
        default: return nil
        }
    }

    private var nameDialogPresented: Binding<Bool> {
        Binding(
            get: {
                switch onEnum(of: state.dialog) {
                case .create, .rename: return true
                default: return false
                }
            },
            set: { isPresented in
                if !isPresented { onEvent(ShoppingListsIntentOnDialogDismissed.shared) }
            }
        )
    }

    private var deleteDialogPresented: Binding<Bool> {
        Binding(
            get: {
                if case .confirmDelete = onEnum(of: state.dialog) { return true }
                return false
            },
            set: { isPresented in
                if !isPresented { onEvent(ShoppingListsIntentOnDialogDismissed.shared) }
            }
        )
    }

    private var deleteTitle: String {
        if case .confirmDelete(let dialog) = onEnum(of: state.dialog) {
            return "Delete “\(dialog.name.localized)”?"
        }
        return ""
    }

    private func confirmNameDialog() {
        let name = nameDraft
        switch onEnum(of: state.dialog) {
        case .rename: onEvent(ShoppingListsIntentOnRenameConfirmed(name: name))
        default: onEvent(ShoppingListsIntentOnCreateConfirmed(name: name))
        }
    }
}

/// A Material-style filter chip: tonal coral with a check when selected, outlined otherwise.
struct FilterChipButton: View {
    let title: String
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 8) {
                if isSelected {
                    Image(systemName: "checkmark").font(.system(size: 13, weight: .semibold))
                }
                Text(title).font(SimmerlyFont.labelLarge)
            }
            .padding(.leading, isSelected ? 8 : 16)
            .padding(.trailing, 16)
            .frame(minHeight: 32)
            .foregroundStyle(isSelected ? SimmerlyColor.onPrimaryContainer : SimmerlyColor.onSurfaceVariant)
            .background(
                isSelected ? SimmerlyColor.primaryContainer : SimmerlyColor.surface,
                in: RoundedRectangle(cornerRadius: 8)
            )
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .strokeBorder(isSelected ? Color.clear : SimmerlyColor.outline, lineWidth: 1)
            )
            .contentShape(Rectangle())
            .frame(minHeight: 44)
        }
        .buttonStyle(.plain)
        .accessibilityAddTraits(isSelected ? .isSelected : [])
    }
}

// MARK: - Previews

private let previewLists: [ShoppingListCardUi] = {
    func text(_ value: String) -> UiText { UiTextDynamic(text: value) }
    let recipe = RecipeThumbUi(recipeId: "r1", name: text("Beef and broccoli"), imageUrl: "")
    return [
        ShoppingListCardUi(
            id: "1", name: text("Tonight's dinner"), ownerId: "u", itemCount: 11, checkedCount: 1,
            progress: 1 / 11, footer: ListCardFooterUiOneRecipe(recipe: recipe, itemCount: 11, quantity: 1)
        ),
        ShoppingListCardUi(
            id: "2", name: text("Week of Oct 26"), ownerId: "u", itemCount: 34, checkedCount: 0,
            progress: 0,
            footer: ListCardFooterUiSeveralRecipes(
                recipes: [recipe, recipe, recipe], recipeCount: 4,
                recipeNames: ["Beef and broccoli", "Shakshuka", "Pad see ew", "Miso salmon"].map(text)
            )
        ),
        ShoppingListCardUi(
            id: "3", name: text("Costco run"), ownerId: "u", itemCount: 9, checkedCount: 6, progress: 6 / 9,
            footer: ListCardFooterUiAddedByHand(
                itemNames: ["Paper towels", "olive oil", "coffee beans"].map(text), itemCount: 9
            )
        ),
        ShoppingListCardUi(
            id: "4", name: text("Sunday brunch"), ownerId: "u", itemCount: 15, checkedCount: 3, progress: 0.2,
            footer: ListCardFooterUiRecipesAndExtras(
                recipes: [recipe, recipe], recipeCount: 2,
                recipeNames: ["Shakshuka", "Lemon ricotta pancakes"].map(text),
                fromRecipesCount: 10, byHandCount: 5
            )
        ),
    ]
}()

private func previewState(lists: [ShoppingListCardUi]?) -> ShoppingListsState {
    ShoppingListsState(
        allLists: lists,
        currentUserId: "u",
        filter: .mine,
        isSearching: false,
        query: "",
        isLoading: false,
        error: nil,
        dialog: ListDialogNone.shared,
        lists: lists ?? []
    )
}

#Preview("Lists") {
    NavigationStack {
        ShoppingListsView(state: previewState(lists: previewLists), onEvent: { _ in })
    }
}

#Preview("Empty") {
    NavigationStack {
        ShoppingListsView(state: previewState(lists: []), onEvent: { _ in })
    }
}

#Preview("Loading") {
    NavigationStack {
        ShoppingListsView(state: previewState(lists: nil), onEvent: { _ in })
    }
}
