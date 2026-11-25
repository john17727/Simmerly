import SwiftUI
import Shared

/// The recipe's timeline: last made, type filters, sort, and the entries on a rail. Mirrors
/// timeline/presentation/TimelineScreen.kt; wide screens keep a single centered column.
struct TimelineView: View {
    let state: TimelineState
    let onEvent: (TimelineIntent) -> Void
    let onEdit: (String) -> Void
    let onRefresh: () async -> Void

    var body: some View {
        Group {
            if state.isLoading {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else if state.hasError {
                ContentUnavailableView {
                    Label("Couldn't load the timeline", systemImage: "exclamationmark.triangle")
                } description: {
                    Text(String(localized: "timeline_load_failed"))
                } actions: {
                    Button("Try again") { onEvent(TimelineIntentRefresh.shared) }
                }
            } else {
                content
            }
        }
        .background(SimmerlyColor.background)
        .navigationTitle("Timeline")
        .navigationSubtitle(state.recipeName)
        .navigationBarTitleDisplayMode(.inline)
        .confirmationDialog(
            "Delete this entry?",
            isPresented: Binding(
                get: { state.pendingDeleteId != nil },
                set: { if !$0 { onEvent(TimelineIntentDeleteDismissed.shared) } }
            ),
            titleVisibility: .visible
        ) {
            Button("Delete entry", role: .destructive) {
                onEvent(TimelineIntentDeleteConfirmed.shared)
            }
        } message: {
            Text("This can't be undone.")
        }
    }

    private var content: some View {
        let events = state.visibleEvents
        return ScrollView {
            LazyVStack(alignment: .leading, spacing: 0) {
                if let lastMade = state.lastMade {
                    lastMadeCard(lastMade)
                }
                filters
                sortRow(count: events.count)
                if events.isEmpty {
                    Text("No events match these filters. Turn a type back on to see more.")
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                        .multilineTextAlignment(.center)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 24)
                        .padding(.horizontal, 16)
                } else {
                    VStack(spacing: 0) {
                        ForEach(Array(events.enumerated()), id: \.element.id) { index, event in
                            TimelineRow(
                                event: event,
                                isLast: index == events.count - 1,
                                onEdit: { onEdit(event.id) },
                                onDelete: { onEvent(TimelineIntentDeleteClicked(eventId: event.id)) }
                            )
                            .onAppear {
                                if event.id == events.last?.id { onEvent(TimelineIntentLoadMore.shared) }
                            }
                        }
                    }
                    .padding(.horizontal, 16)
                    .padding(.top, 8)
                }
                if state.isLoadingMore {
                    ProgressView().frame(maxWidth: .infinity).padding(.vertical, 8)
                }
            }
            .padding(.bottom, 24)
            .frame(maxWidth: 640)
            .frame(maxWidth: .infinity)
        }
        .refreshable { await onRefresh() }
    }

    private func lastMadeCard(_ lastMade: String) -> some View {
        VStack(alignment: .leading, spacing: 2) {
            Text("Last made")
                .font(SimmerlyFont.labelMedium)
                .tracking(0.5)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            Text(lastMade).font(SimmerlyFont.titleMedium)
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(SimmerlyColor.surfaceContainerLow, in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
        .padding(.horizontal, 16)
        .padding(.top, 4)
    }

    private var filters: some View {
        ScrollView(.horizontal, showsIndicators: false) {
            HStack(spacing: 8) {
                ForEach([TimelineEventType.comment, .info, .system], id: \.self) { type in
                    let selected = state.visibleTypes.contains(type)
                    Button {
                        onEvent(TimelineIntentToggleType(type: type))
                    } label: {
                        HStack(spacing: 8) {
                            if selected {
                                Image(systemName: "checkmark").font(.system(size: 14, weight: .semibold))
                            }
                            Text(type.title)
                        }
                        .font(SimmerlyFont.labelLarge)
                        .foregroundStyle(selected ? SimmerlyColor.onPrimaryContainer : SimmerlyColor.onSurfaceVariant)
                        .padding(.leading, selected ? 8 : 16)
                        .padding(.trailing, 16)
                        .frame(height: 32)
                        .background(selected ? SimmerlyColor.primaryContainer : Color.clear, in: RoundedRectangle(cornerRadius: 8))
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .strokeBorder(SimmerlyColor.outline, lineWidth: selected ? 0 : 1)
                        )
                    }
                    .buttonStyle(.plain)
                    .accessibilityAddTraits(selected ? .isSelected : [])
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 8)
        }
    }

    private func sortRow(count: Int) -> some View {
        HStack {
            Text(count == 1 ? "1 event" : "\(count) events")
                .font(SimmerlyFont.labelMedium)
                .tracking(0.5)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            Spacer()
            Button {
                onEvent(TimelineIntentToggleSort.shared)
            } label: {
                Label(
                    state.newestFirst ? "Newest first" : "Oldest first",
                    systemImage: state.newestFirst ? "arrow.down" : "arrow.up"
                )
                .font(SimmerlyFont.labelLarge)
                .foregroundStyle(SimmerlyColor.primary)
                .padding(.horizontal, 12)
                .frame(height: 40)
            }
        }
        .padding(.leading, 16)
        .padding(.trailing, 8)
        .padding(.vertical, 4)
    }
}

extension TimelineEventType {
    var title: String {
        switch self {
        case .info: return "Info"
        case .comment: return "Comment"
        case .system: return "System"
        }
    }

    var symbol: String {
        switch self {
        case .info: return "info.circle"
        case .comment: return "fork.knife"
        case .system: return "gearshape"
        }
    }

    var dotBackground: Color {
        switch self {
        case .info: return SimmerlyColor.tertiaryContainer
        case .comment: return SimmerlyColor.primaryContainer
        case .system: return SimmerlyColor.surfaceContainer
        }
    }

    var dotForeground: Color {
        switch self {
        case .info: return SimmerlyColor.onTertiaryContainer
        case .comment: return SimmerlyColor.onPrimaryContainer
        case .system: return SimmerlyColor.onSurfaceVariant
        }
    }
}

/// A type's dot, shared with the edit screen's header.
struct TimelineTypeDot: View {
    let type: TimelineEventType

    var body: some View {
        Image(systemName: type.symbol)
            .font(.system(size: 15, weight: .medium))
            .foregroundStyle(type.dotForeground)
            .frame(width: 32, height: 32)
            .background(type.dotBackground, in: Circle())
            .accessibilityHidden(true)
    }
}

private struct TimelineRow: View {
    let event: TimelineEventUi
    let isLast: Bool
    let onEdit: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(alignment: .top, spacing: 12) {
            VStack(spacing: 0) {
                TimelineTypeDot(type: event.type)
                Rectangle()
                    .fill(isLast ? Color.clear : SimmerlyColor.outlineVariant)
                    .frame(width: 2)
                    .padding(.vertical, 4)
                    .frame(maxHeight: .infinity)
            }
            if event.type == .system {
                compact
            } else {
                card
            }
        }
        .fixedSize(horizontal: false, vertical: true)
    }

    private var compact: some View {
        VStack(alignment: .leading, spacing: 2) {
            Text(event.subject)
                .font(SimmerlyFont.bodyMedium)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            Text(event.date)
                .font(SimmerlyFont.bodySmall)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity, minHeight: 32, alignment: .topLeading)
        .padding(.top, 6)
        .padding(.bottom, 16)
    }

    private var card: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(spacing: 12) {
                Text(event.authorInitials)
                    .font(.custom("DM Serif Display", size: 13))
                    .foregroundStyle(SimmerlyColor.onPrimaryContainer)
                    .frame(width: 32, height: 32)
                    .background(SimmerlyColor.primaryContainer, in: Circle())
                    .accessibilityHidden(true)
                VStack(alignment: .leading, spacing: 0) {
                    Text(event.subject).font(SimmerlyFont.titleSmall)
                    Text(event.date)
                        .font(SimmerlyFont.bodySmall)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
                .frame(maxWidth: .infinity, alignment: .leading)
                if event.canEdit {
                    Menu {
                        Button("Edit", systemImage: "pencil", action: onEdit)
                        Button("Delete", systemImage: "trash", role: .destructive, action: onDelete)
                    } label: {
                        Image(systemName: "ellipsis")
                            .rotationEffect(.degrees(90))
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                            .frame(width: 40, height: 40)
                    }
                    .padding(.vertical, -4)
                    .padding(.trailing, -8)
                    .accessibilityLabel("Event options")
                }
            }
            if let image = event.image {
                RemoteImage(url: image, targetSize: CGSize(width: 300, height: 180)) {
                    SimmerlyColor.surfaceContainer
                }
                .frame(height: 180)
                .frame(maxWidth: .infinity)
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            if let message = event.message, !message.isEmpty {
                Text(message)
                    .font(SimmerlyFont.bodyMedium)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(SimmerlyColor.surfaceContainerLow, in: RoundedRectangle(cornerRadius: 12))
        .overlay(RoundedRectangle(cornerRadius: 12).strokeBorder(SimmerlyColor.outlineVariant, lineWidth: 1))
        .padding(.bottom, 16)
    }
}
