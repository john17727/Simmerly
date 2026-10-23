import SwiftUI
import Shared

/// The pinned field for adding items. Mirrors shoppinglists/presentation/detail/AddItemBar.kt:
/// free text is resolved by the ViewModel (suggestion, exact match, parser, or plain note), so this
/// only reports typing, focus and submits.
struct AddItemBar: View {
    @Binding var text: String
    let isAdding: Bool
    var focused: FocusState<Bool>.Binding
    let onSubmit: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            Rectangle().fill(SimmerlyColor.outlineVariant).frame(height: 1)
            HStack(spacing: 8) {
                HStack(spacing: 4) {
                    TextField("Add an item, like 2 lb chicken thighs", text: $text)
                        .font(SimmerlyFont.bodyLarge)
                        .textInputAutocapitalization(.sentences)
                        .submitLabel(.done)
                        .focused(focused)
                        .onSubmit(onSubmit)
                        .accessibilityLabel("Add an item")
                    if !text.isEmpty {
                        Button {
                            text = ""
                        } label: {
                            Image(systemName: "xmark")
                                .font(.system(size: 14, weight: .medium))
                                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                                .frame(width: 40, height: 40)
                        }
                        .buttonStyle(.plain)
                        .accessibilityLabel("Clear")
                    }
                }
                .padding(.leading, 20)
                .padding(.trailing, 4)
                .frame(height: 48)
                .background(SimmerlyColor.surfaceContainer, in: Capsule())
                .overlay(
                    Capsule().strokeBorder(
                        focused.wrappedValue ? SimmerlyColor.primary : Color.clear,
                        lineWidth: 2
                    )
                )
                Button(action: onSubmit) {
                    Group {
                        if isAdding {
                            ProgressView().tint(SimmerlyColor.onPrimaryContainer)
                        } else {
                            Image(systemName: "plus").font(.system(size: 20, weight: .medium))
                        }
                    }
                    .foregroundStyle(SimmerlyColor.onPrimaryContainer)
                    .frame(width: 48, height: 48)
                    .background(SimmerlyColor.primaryContainer, in: Circle())
                    .opacity(canSubmit ? 1 : 0.5)
                }
                .buttonStyle(.plain)
                .disabled(!canSubmit)
                .accessibilityLabel("Add item")
            }
            .padding(.horizontal, 16)
            .padding(.top, 12)
            .padding(.bottom, 12)
        }
        .background(SimmerlyColor.background)
    }

    private var canSubmit: Bool {
        !text.trimmingCharacters(in: .whitespaces).isEmpty && !isAdding
    }
}

/// Foods matching what's being typed, floating above the add bar.
struct AddItemSuggestions: View {
    let suggestions: [FoodOptionUi]
    let query: String
    let highlightedId: String?
    let onPicked: (FoodOptionUi) -> Void

    /// Six rows show at once; any more scroll inside the card.
    private let rowHeight: CGFloat = 48
    private let visibleRows = 6

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(suggestions.count == 1 ? "1 matching ingredient" : "\(suggestions.count) matching ingredients")
                .font(SimmerlyFont.labelMedium)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                .padding(.horizontal, 16)
                .padding(.top, 12)
                .padding(.bottom, 4)
            ScrollView {
                LazyVStack(spacing: 0) {
                    ForEach(suggestions, id: \.id) { food in
                        Button {
                            onPicked(food)
                        } label: {
                            HStack(spacing: 12) {
                                highlightMatch(food.name.localized)
                                    .font(SimmerlyFont.bodyLarge)
                                    .foregroundStyle(SimmerlyColor.onSurface)
                                    .frame(maxWidth: .infinity, alignment: .leading)
                                Text(food.aisle?.localized ?? "No aisle")
                                    .font(SimmerlyFont.bodySmall)
                                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                            }
                            .padding(.horizontal, 16)
                            .frame(minHeight: rowHeight)
                            .background(food.id == highlightedId ? SimmerlyColor.surfaceContainer : Color.clear)
                            .contentShape(Rectangle())
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.bottom, 4)
            }
            .frame(maxHeight: rowHeight * CGFloat(min(suggestions.count, visibleRows)) + 4)
            .accessibilityLabel("Matching ingredients")
        }
        // The shadow sits on the card's shape only, so the rows inside don't each cast one.
        .background {
            RoundedRectangle(cornerRadius: 12)
                .fill(SimmerlyColor.surface)
                .shadow(color: .black.opacity(0.10), radius: 6, y: 4)
        }
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
    }

    /// `name` with the first occurrence of `query` in bold, as the design marks the match.
    private func highlightMatch(_ name: String) -> Text {
        let term = query.trimmingCharacters(in: .whitespaces)
        guard !term.isEmpty, let range = name.range(of: term, options: .caseInsensitive) else {
            return Text(name)
        }
        let before = String(name[..<range.lowerBound])
        let match = String(name[range])
        let after = String(name[range.upperBound...])
        return Text("\(before)\(Text(match).bold())\(after)")
    }
}

// MARK: - Previews

private struct AddItemBarPreview: View {
    @State var text: String
    var isAdding = false
    @FocusState private var focused: Bool

    var body: some View {
        VStack {
            Spacer()
            AddItemBar(text: $text, isAdding: isAdding, focused: $focused, onSubmit: {})
        }
    }
}

#Preview("Empty") {
    AddItemBarPreview(text: "")
}

#Preview("Typing") {
    AddItemBarPreview(text: "2 cups milk")
}

#Preview("Adding") {
    AddItemBarPreview(text: "2 cups milk", isAdding: true)
}

#Preview("Suggestions") {
    func food(_ id: String, _ name: String, _ aisle: String?) -> FoodOptionUi {
        FoodOptionUi(id: id, name: UiTextDynamic(text: name), aisle: aisle.map { UiTextDynamic(text: $0) })
    }
    return AddItemSuggestions(
        suggestions: [
            food("1", "milk", "Dairy and eggs"),
            food("2", "milk cream", "Dairy and eggs"),
            food("3", "milk powder", "Baking"),
            food("4", "milk breads", "Bakery"),
            food("5", "milkfats", nil),
            food("6", "nut milk", "Dairy and eggs"),
            food("7", "oat milk", "Dairy and eggs"),
        ],
        query: "milk",
        highlightedId: "1",
        onPicked: { _ in }
    )
    .padding()
}
