import SwiftUI
import Shared

/// Mirrors shoppinglists/presentation/detail/ShoppingListDetailScreen.kt: the list name as a large
/// title, the progress summary, linked recipes, collapsible aisle or recipe sections and the
/// checked card. The add-item field lives in the tab view's bottom accessory (`AddItemAccessory`);
/// this screen only shows its matching-ingredients card.
struct ShoppingListDetailView: View {
    let state: ShoppingListDetailState
    /// Whether the add-item field is open above the keyboard.
    let addFieldFocused: Bool
    @Binding var snackbar: SnackbarMessage?
    let onEvent: (ShoppingListDetailIntent) -> Void
    @State private var scrollPosition = ScrollPosition(edge: .top)
    @State private var isScrolledDown = false
    @State private var renameDraft = ""


    var body: some View {
        content
            .navigationTitle(state.name?.localized ?? "")
            // Pushed screens otherwise settle inline while the loading spinner shows, so the
            // title wouldn't start large and collapse on scroll like the lists screen.
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Menu {
                        Button("Rename", systemImage: "pencil") { onEvent(ShoppingListDetailIntentOnRenameClicked.shared) }
                        Button("Delete list", systemImage: "trash", role: .destructive) {
                            onEvent(ShoppingListDetailIntentOnDeleteListClicked.shared)
                        }
                    } label: {
                        Image(systemName: "ellipsis")
                            .rotationEffect(.degrees(90))
                    }
                    .accessibilityLabel("List options")
                }
            }
            .alert("Rename list", isPresented: renamePresented) {
                TextField("List name", text: $renameDraft)
                    .textInputAutocapitalization(.sentences)
                Button("Save") { onEvent(ShoppingListDetailIntentOnRenameConfirmed(name: renameDraft)) }
                    .disabled(renameDraft.trimmingCharacters(in: .whitespaces).isEmpty)
                Button("Cancel", role: .cancel) { onEvent(ShoppingListDetailIntentOnDialogDismissed.shared) }
            }
            .onChange(of: renameSeed) { _, seed in renameDraft = seed ?? "" }
            .alert(
                "Delete “\(state.name?.localized ?? "")”?",
                isPresented: deletePresented
            ) {
                Button("Delete", role: .destructive) { onEvent(ShoppingListDetailIntentOnDeleteListConfirmed.shared) }
                Button("Cancel", role: .cancel) { onEvent(ShoppingListDetailIntentOnDialogDismissed.shared) }
            } message: {
                Text("The list and all of its items will be removed for everyone in your household.")
            }
    }

    @ViewBuilder
    private var content: some View {
        if state.isLoading {
            ProgressView()
                .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else if state.name == nil, let error = state.error {
            ContentUnavailableView {
                Label(error.localized, systemImage: "cart")
            } actions: {
                Button("Try again") { onEvent(ShoppingListDetailIntentOnRefresh.shared) }
            }
        } else {
            list
        }
    }

    private var list: some View {
        ScrollView {
            LazyVStack(alignment: .leading, spacing: 0) {
                summary
                if !state.linkedRecipes.isEmpty {
                    LinkedRecipesCard(recipes: state.linkedRecipes, onEvent: onEvent)
                        .padding(.horizontal, 16)
                }
                if state.sections.isEmpty && state.checkedItems.isEmpty {
                    Text("Nothing on this list yet. Add your first item below.")
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                        .padding(.horizontal, 32)
                        .padding(.vertical, 48)
                } else {
                    groupingPicker
                    if !state.sections.isEmpty {
                        SectionsControl(
                            sectionCount: state.sections.count,
                            grouping: state.grouping,
                            anyExpanded: state.anySectionExpanded,
                            onEvent: onEvent
                        )
                    }
                    ForEach(Array(state.sections.enumerated()), id: \.element.key) { index, section in
                        ItemSection(
                            section: section,
                            isFirst: index == 0,
                            collapsed: state.collapsedSections.contains(section.key),
                            onEvent: onEvent
                        )
                    }
                    if !state.checkedItems.isEmpty {
                        CheckedCard(items: state.checkedItems, expanded: state.checkedExpanded, onEvent: onEvent)
                            .padding(.horizontal, 16)
                            .padding(.top, 16)
                    }
                }
            }
            .padding(.bottom, 24)
        }
        .scrollPosition($scrollPosition)
        // Long lists only: the button appears once the summary has scrolled out of view.
        .onScrollGeometryChange(for: Bool.self) { geometry in
            geometry.contentOffset.y + geometry.contentInsets.top > 80
        } action: { _, scrolledDown in
            isScrolledDown = scrolledDown
        }
        .refreshable { onEvent(ShoppingListDetailIntentOnRefresh.shared) }
        .overlay(alignment: .bottomTrailing) {
            // Hidden while the add-item field is open, so it doesn't crowd the keyboard.
            if isScrolledDown && !addFieldFocused {
                ScrollToTopButton { withAnimation { scrollPosition.scrollTo(edge: .top) } }
                    .padding(.trailing, 16)
                    // Sits above the snackbar while one is showing, as in the design.
                    .padding(.bottom, snackbar == nil ? 16 : 76)
                    .transition(.scale.combined(with: .opacity))
            }
        }
        .animation(.snappy, value: isScrolledDown)
        .animation(.snappy, value: snackbar)
        .snackbar($snackbar)

    }

    private var summary: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(progressSummary)
                .font(SimmerlyFont.bodyMedium)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            ListProgressBar(progress: Double(state.progress))
        }
        .padding(.horizontal, 16)
        .padding(.bottom, 16)
    }

    private var progressSummary: String {
        let remaining = Int(state.remainingCount)
        let checked = Int(state.checkedCount)
        if remaining == 0 && checked == 0 { return "No items yet" }
        if remaining == 0 { return "All \(checked) checked" }
        return "\(remaining) to get · \(checked) checked"
    }

    private var groupingPicker: some View {
        Picker(
            "Group items by",
            selection: Binding(
                get: { state.grouping },
                set: { onEvent(ShoppingListDetailIntentOnGroupingSelected(grouping: $0)) }
            )
        ) {
            Text("By aisle").tag(ItemGrouping.byAisle)
            Text("By recipe").tag(ItemGrouping.byRecipe)
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 16)
        .padding(.top, 16)
        .padding(.bottom, 4)
    }

    // MARK: - Dialogs

    private var renameSeed: String? {
        if case .rename(let rename) = onEnum(of: state.dialog) { return rename.currentName }
        return nil
    }

    private var renamePresented: Binding<Bool> {
        Binding(
            get: { renameSeed != nil },
            set: { if !$0 { onEvent(ShoppingListDetailIntentOnDialogDismissed.shared) } }
        )
    }

    private var deletePresented: Binding<Bool> {
        Binding(
            get: {
                if case .confirmDelete = onEnum(of: state.dialog) { return true }
                return false
            },
            set: { if !$0 { onEvent(ShoppingListDetailIntentOnDialogDismissed.shared) } }
        )
    }
}

// MARK: - Linked recipes

private struct LinkedRecipesCard: View {
    let recipes: [LinkedRecipeUi]
    let onEvent: (ShoppingListDetailIntent) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("Linked recipes")
                .font(SimmerlyFont.titleSmall)
                .foregroundStyle(SimmerlyColor.onSurface.opacity(0.8))
                .padding(.bottom, 4)
            ForEach(Array(recipes.enumerated()), id: \.element.recipeId) { index, recipe in
                if index > 0 {
                    Rectangle().fill(SimmerlyColor.outlineVariant).frame(height: 1)
                }
                HStack(spacing: 12) {
                    RecipeThumbnail(imageUrl: recipe.imageUrl, size: 40)
                        .accessibilityLabel("Recipe photo, \(recipe.name.localized)")
                    VStack(alignment: .leading, spacing: 0) {
                        Text(recipe.name.localized)
                            .font(SimmerlyFont.titleSmall)
                            .foregroundStyle(SimmerlyColor.onSurface)
                            .lineLimit(1)
                        Text(summary(recipe))
                            .font(SimmerlyFont.bodySmall)
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    QuantityStepper(
                        quantity: Int(recipe.quantity),
                        recipeName: recipe.name.localized,
                        onDecrease: {
                            onEvent(ShoppingListDetailIntentOnRecipeQuantityChanged(recipeId: recipe.recipeId, delta: -1))
                        },
                        onIncrease: {
                            onEvent(ShoppingListDetailIntentOnRecipeQuantityChanged(recipeId: recipe.recipeId, delta: 1))
                        }
                    )
                }
                .padding(.vertical, 8)
            }
        }
        .padding(.horizontal, 16)
        .padding(.top, 12)
        .padding(.bottom, 8)
        .background(SimmerlyColor.surfaceContainerLow, in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
    }

    private func summary(_ recipe: LinkedRecipeUi) -> String {
        var text = recipe.itemCount == 1 ? "1 item" : "\(recipe.itemCount) items"
        if recipe.quantity == 2 {
            text += " · doubled"
        } else if recipe.quantity > 2 {
            text += " · ×\(recipe.quantity)"
        }
        return text
    }
}

private struct QuantityStepper: View {
    let quantity: Int
    let recipeName: String
    let onDecrease: () -> Void
    let onIncrease: () -> Void

    var body: some View {
        HStack(spacing: 0) {
            Button(action: onDecrease) {
                Image(systemName: "minus").frame(width: 44, height: 44)
            }
            .accessibilityLabel(quantity <= 1 ? "Remove \(recipeName) from this list" : "Fewer servings of \(recipeName)")
            Text("\(quantity)")
                .font(SimmerlyFont.titleSmall)
                .frame(minWidth: 20)
                .accessibilityLabel("Quantity \(quantity)")
            Button(action: onIncrease) {
                Image(systemName: "plus").frame(width: 44, height: 44)
            }
            .accessibilityLabel("More servings of \(recipeName)")
        }
        .buttonStyle(.plain)
        .foregroundStyle(SimmerlyColor.onSurface)
        .background(SimmerlyColor.surface, in: Capsule())
        .overlay(Capsule().strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
    }
}

// MARK: - Sections

private struct SectionsControl: View {
    let sectionCount: Int
    let grouping: ItemGrouping
    let anyExpanded: Bool
    let onEvent: (ShoppingListDetailIntent) -> Void

    var body: some View {
        HStack(spacing: 4) {
            Text(countLabel)
                .font(SimmerlyFont.labelMedium)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                .frame(maxWidth: .infinity, alignment: .leading)
            // Only shown while there are unchecked items, so it never needs a disabled state.
            Button {
                onEvent(ShoppingListDetailIntentOnCheckAllClicked.shared)
            } label: {
                Label("Check all", systemImage: "checkmark.circle")
            }
            Button {
                onEvent(ShoppingListDetailIntentOnToggleAllSectionsClicked.shared)
            } label: {
                Label(
                    anyExpanded ? "Collapse all" : "Expand all",
                    systemImage: anyExpanded ? "arrow.down.right.and.arrow.up.left" : "arrow.up.left.and.arrow.down.right"
                )
            }
        }
        .buttonStyle(.plain)
        .font(SimmerlyFont.labelLarge)
        .foregroundStyle(SimmerlyColor.onSurface.opacity(0.8))
        .labelStyle(CompactLabelStyle())
        .padding(.leading, 16)
        .padding(.trailing, 8)
        .padding(.top, 8)
    }

    private var countLabel: String {
        switch grouping {
        case .byAisle: return sectionCount == 1 ? "1 aisle" : "\(sectionCount) aisles"
        case .byRecipe: return sectionCount == 1 ? "1 section" : "\(sectionCount) sections"
        }
    }
}

private struct CompactLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 6) {
            configuration.icon
            configuration.title
        }
        .padding(.horizontal, 8)
        .frame(minHeight: 44)
        .contentShape(Rectangle())
    }
}

private struct ItemSection: View {
    let section: ItemSectionUi
    let isFirst: Bool
    let collapsed: Bool
    let onEvent: (ShoppingListDetailIntent) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            if !isFirst {
                Rectangle().fill(SimmerlyColor.outlineVariant).frame(height: 1)
                    .padding(.leading, 16)
            }
            // The whole header row toggles the section, so the target is far larger than the icon.
            Button {
                onEvent(ShoppingListDetailIntentOnSectionToggled(sectionKey: section.key))
            } label: {
                HStack(spacing: 8) {
                    Text(title)
                        .font(SimmerlyFont.titleSmall)
                        .foregroundStyle(SimmerlyColor.onSurface.opacity(0.8))
                    Text("\(section.items.count)")
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    Spacer()
                    Image(systemName: collapsed ? "chevron.down" : "chevron.up")
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
                .padding(.horizontal, 16)
                .padding(.top, 8)
                .frame(minHeight: 48)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityValue(collapsed ? "Collapsed" : "Expanded")
            if !collapsed {
                ForEach(Array(section.items.enumerated()), id: \.element.id) { index, item in
                    if index > 0 {
                        Rectangle().fill(SimmerlyColor.surfaceContainer).frame(height: 1)
                            .padding(.leading, 16)
                    }
                    ItemRow(
                        item: item,
                        onToggle: { onEvent(ShoppingListDetailIntentOnItemCheckedChanged(itemId: item.id, checked: true)) },
                        onEdit: { onEvent(ShoppingListDetailIntentOnEditItemClicked(itemId: item.id)) }
                    )
                }
            }
        }
    }

    private var title: String {
        switch section.kind {
        case .aisle, .recipe: return section.title.localized
        case .noAisle: return "No aisle"
        case .addedByHand: return "Added by hand"
        }
    }
}

private struct ItemRow: View {
    let item: ShoppingItemUi
    let onToggle: () -> Void
    let onEdit: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 4) {
            Button(action: onToggle) {
                HStack(alignment: .top, spacing: 16) {
                    SimmerlyCheckbox(checked: item.checked)
                        .padding(.top, 1)
                    VStack(alignment: .leading, spacing: 2) {
                        itemText(item)
                            .font(SimmerlyFont.bodyLarge)
                            .foregroundStyle(SimmerlyColor.onSurface)
                        if let note = item.note {
                            Text(note.localized)
                                .font(SimmerlyFont.bodySmall)
                                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        }
                        if let attribution = item.attribution {
                            Attribution(text: attribution.localized)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
                .padding(.vertical, 12)
                .contentShape(Rectangle())
            }
            .buttonStyle(.plain)
            .accessibilityValue(item.checked ? "Checked" : "Unchecked")
            Button(action: onEdit) {
                Image(systemName: "pencil")
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    .frame(width: 44, height: 44)
            }
            .buttonStyle(.plain)
            .accessibilityLabel("Edit \(item.name.localized)")
        }
        .padding(.leading, 16)
        .padding(.trailing, 4)
    }
}

/// "qty **food**" for food items, plain text for note-only ones.
private func itemText(_ item: ShoppingItemUi) -> Text {
    let quantity = item.quantityText.localized
    let name = Text(item.name.localized).fontWeight(item.nameIsFood ? .semibold : .regular)
    return quantity.isEmpty ? name : Text("\(quantity) \(name)")
}

private struct Attribution: View {
    let text: String
    var muted = false

    var body: some View {
        Label(text, systemImage: "fork.knife")
            .font(SimmerlyFont.labelMedium)
            .foregroundStyle(muted ? SimmerlyColor.onSurfaceVariant : SimmerlyColor.onTertiaryContainer)
            .labelStyle(AttributionLabelStyle())
    }
}

private struct AttributionLabelStyle: LabelStyle {
    func makeBody(configuration: Configuration) -> some View {
        HStack(spacing: 4) {
            configuration.icon.imageScale(.small)
            configuration.title
        }
    }
}

// MARK: - Checked items

private struct CheckedCard: View {
    let items: [ShoppingItemUi]
    let expanded: Bool
    let onEvent: (ShoppingListDetailIntent) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 4) {
                Text("\(items.count) checked")
                    .font(SimmerlyFont.titleSmall)
                    .foregroundStyle(SimmerlyColor.onSurface.opacity(0.8))
                    .frame(maxWidth: .infinity, alignment: .leading)
                Button("Uncheck all") { onEvent(ShoppingListDetailIntentOnUncheckAllClicked.shared) }
                    .font(SimmerlyFont.titleSmall)
                    .foregroundStyle(SimmerlyColor.onPrimaryContainer)
                    .padding(.horizontal, 8)
                    .frame(minHeight: 44)
                Button {
                    onEvent(ShoppingListDetailIntentOnDeleteCheckedClicked.shared)
                } label: {
                    Image(systemName: "trash").frame(width: 44, height: 44)
                }
                .accessibilityLabel("Delete checked items")
                Button {
                    onEvent(ShoppingListDetailIntentOnCheckedExpandedToggled.shared)
                } label: {
                    Image(systemName: expanded ? "chevron.up" : "chevron.down").frame(width: 44, height: 44)
                }
                .accessibilityLabel(expanded ? "Hide checked items" : "Show checked items")
            }
            .buttonStyle(.plain)
            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            .padding(.leading, 16)
            .padding(.trailing, 4)
            .padding(.vertical, 2)
            if expanded {
                Rectangle().fill(SimmerlyColor.outlineVariant).frame(height: 1)
                VStack(alignment: .leading, spacing: 0) {
                    ForEach(Array(items.enumerated()), id: \.element.id) { index, item in
                        if index > 0 {
                            Rectangle().fill(SimmerlyColor.outlineVariant).frame(height: 1)
                        }
                        Button {
                            onEvent(ShoppingListDetailIntentOnItemCheckedChanged(itemId: item.id, checked: false))
                        } label: {
                            HStack(alignment: .top, spacing: 16) {
                                SimmerlyCheckbox(checked: true).padding(.top, 1)
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(
                                        [item.quantityText.localized, item.name.localized]
                                            .filter { !$0.isEmpty }
                                            .joined(separator: " ")
                                    )
                                    .font(SimmerlyFont.bodyLarge)
                                    .strikethrough()
                                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                                    if let attribution = item.attribution {
                                        Attribution(text: attribution.localized, muted: true)
                                    }
                                }
                                .frame(maxWidth: .infinity, alignment: .leading)
                            }
                            .padding(.vertical, 12)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                        .accessibilityValue("Checked")
                    }
                }
                .padding(.horizontal, 16)
                .padding(.bottom, 8)
            }
        }
        .background(SimmerlyColor.surfaceContainerLow, in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
    }
}

/// The small outlined button that jumps back to the top of a long list.
private struct ScrollToTopButton: View {
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Image(systemName: "arrow.up")
                .font(.system(size: 18, weight: .medium))
                .foregroundStyle(SimmerlyColor.onSurface.opacity(0.8))
                .frame(width: 48, height: 48)
                .background {
                    RoundedRectangle(cornerRadius: 16)
                        .fill(SimmerlyColor.surface)
                        .shadow(color: .black.opacity(0.12), radius: 6, y: 4)
                }
                .overlay(RoundedRectangle(cornerRadius: 16).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
        }
        .buttonStyle(.plain)
        .accessibilityLabel("Scroll to top")
    }
}

// MARK: - Previews

private func text(_ value: String) -> UiText { UiTextDynamic(text: value) }

private func previewItem(
    _ id: String, _ quantity: String, _ name: String,
    note: String? = nil, from: String? = nil, checked: Bool = false
) -> ShoppingItemUi {
    ShoppingItemUi(
        id: id, quantityText: text(quantity), name: text(name), nameIsFood: true,
        note: note.map(text), attribution: from.map(text), checked: checked
    )
}

private func previewState(collapsed: Set<String> = [], empty: Bool = false) -> ShoppingListDetailState {
    ShoppingListDetailState(
        isLoading: false,
        error: nil,
        name: text("Sunday brunch"),
        remainingCount: empty ? 0 : 12,
        checkedCount: empty ? 0 : 3,
        progress: empty ? 0 : 0.2,
        linkedRecipes: empty ? [] : [
            LinkedRecipeUi(recipeId: "r1", name: text("Shakshuka"), imageUrl: "", quantity: 1, itemCount: 6),
            LinkedRecipeUi(recipeId: "r2", name: text("Lemon ricotta pancakes"), imageUrl: "", quantity: 2, itemCount: 5),
        ],
        grouping: .byAisle,
        sections: empty ? [] : [
            ItemSectionUi(key: "produce", kind: .aisle, title: text("Produce"), items: [
                previewItem("1", "1", "yellow onion", note: "diced", from: "Shakshuka"),
                previewItem("2", "4", "lemons", note: "zested and juiced", from: "Lemon ricotta pancakes ×2"),
                previewItem("3", "1 pint", "strawberries"),
            ]),
            ItemSectionUi(key: "none", kind: .noAisle, title: text(""), items: [
                previewItem("4", "2 tsp", "baking powder"),
            ]),
        ],
        collapsedSections: collapsed,
        checkedItems: empty ? [] : [previewItem("5", "1 tsp", "ground cumin", from: "Shakshuka", checked: true)],
        checkedExpanded: true,
        addText: "",
        isAdding: false,
        addSuggestions: [],
        addHighlightedId: nil,
        addSuggestionQuery: "",
        editor: nil,
        units: [],
        foodSuggestions: [],
        dialog: DetailDialogNone.shared
    )
}

#Preview("List") {
    NavigationStack {
        ShoppingListDetailView(
            state: previewState(), addFieldFocused: false, snackbar: .constant(nil), onEvent: { _ in }
        )
    }
}

#Preview("Collapsed") {
    NavigationStack {
        ShoppingListDetailView(
            state: previewState(collapsed: ["produce"]), addFieldFocused: false, snackbar: .constant(nil),
            onEvent: { _ in }
        )
    }
}

#Preview("Empty") {
    NavigationStack {
        ShoppingListDetailView(
            state: previewState(empty: true), addFieldFocused: false, snackbar: .constant(nil), onEvent: { _ in }
        )
    }
}
