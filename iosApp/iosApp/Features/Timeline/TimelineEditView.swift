import SwiftUI
import PhotosUI
import Shared

/// Edits one entry's subject, message and photo. Mirrors the mobile TimelineEdit design: close and
/// Save in the header, floating-label fields, a photo section and Delete entry at the bottom.
/// Leaving without saving discards the changes. (No "Take photo": camera capture is Android-only.)
struct TimelineEditView: View {
    let state: TimelineEditState
    let onEvent: (TimelineEditIntent) -> Void

    @Environment(\.dismiss) private var dismiss
    @State private var photoItem: PhotosPickerItem?
    @State private var pickedPreview: UIImage?

    var body: some View {
        Group {
            if !state.isLoaded {
                ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                form
            }
        }
        .background(SimmerlyColor.background)
        .navigationTitle("Edit entry")
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button { dismiss() } label: { Image(systemName: "xmark") }
                    .accessibilityLabel("Close without saving")
            }
            ToolbarItem(placement: .confirmationAction) {
                Button("Save") { onEvent(TimelineEditIntentSave.shared) }
                    .disabled(!state.canSave)
                    .tint(SimmerlyColor.primary)
            }
        }
        .confirmationDialog(
            "Delete this entry?",
            isPresented: Binding(
                get: { state.showDeleteConfirm },
                set: { if !$0 { onEvent(TimelineEditIntentDeleteDismissed.shared) } }
            ),
            titleVisibility: .visible
        ) {
            Button("Delete entry", role: .destructive) {
                onEvent(TimelineEditIntentDeleteConfirmed.shared)
            }
        } message: {
            Text("This can't be undone.")
        }
        .onChange(of: photoItem) { _, item in
            guard let item else { return }
            Task { await loadPhoto(item) }
        }
    }

    private var form: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 24) {
                HStack(spacing: 12) {
                    TimelineTypeDot(type: state.type)
                    VStack(alignment: .leading, spacing: 0) {
                        Text("\(state.type.title) · \(state.date)").font(SimmerlyFont.titleSmall)
                        Text(state.recipeName)
                            .font(SimmerlyFont.bodySmall)
                            .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                            .lineLimit(1)
                    }
                }

                OutlinedField(label: "Subject") {
                    TextField(
                        "",
                        text: Binding(
                            get: { state.subject },
                            set: { onEvent(TimelineEditIntentSubjectChanged(text: $0)) }
                        )
                    )
                    .font(SimmerlyFont.bodyLarge)
                    .padding(.horizontal, 16)
                    .frame(height: 56)
                }

                OutlinedField(label: "Message") {
                    ZStack(alignment: .topLeading) {
                        TextEditor(
                            text: Binding(
                                get: { state.message },
                                set: { onEvent(TimelineEditIntentMessageChanged(text: $0)) }
                            )
                        )
                        .font(SimmerlyFont.bodyLarge)
                        .scrollContentBackground(.hidden)
                        .padding(.horizontal, 11)
                        .padding(.vertical, 8)
                        if state.message.isEmpty {
                            Text("What happened this time?")
                                .font(SimmerlyFont.bodyLarge)
                                .foregroundStyle(SimmerlyColor.onSurfaceVariant.opacity(0.7))
                                .padding(16)
                                .allowsHitTesting(false)
                        }
                    }
                    .frame(height: 136)
                }

                photoSection

                VStack(alignment: .leading, spacing: 0) {
                    Divider().overlay(SimmerlyColor.outlineVariant)
                    Button(role: .destructive) {
                        onEvent(TimelineEditIntentDeleteClicked.shared)
                    } label: {
                        Label("Delete entry", systemImage: "trash")
                            .font(SimmerlyFont.labelLarge)
                            .foregroundStyle(SimmerlyColor.error)
                            .padding(.horizontal, 12)
                            .frame(height: 48)
                    }
                    .padding(.top, 8)
                    .disabled(state.isDeleting || state.isSaving)
                }
            }
            .padding(.horizontal, 16)
            .padding(.top, 16)
            .padding(.bottom, 32)
            .frame(maxWidth: 640)
            .frame(maxWidth: .infinity)
        }
        .disabled(state.isSaving)
    }

    private var photoSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Photo")
                .font(SimmerlyFont.labelMedium)
                .tracking(0.5)
                .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            if state.hasPhoto {
                VStack(alignment: .leading, spacing: 12) {
                    photoPreview
                        .frame(height: 220)
                        .frame(maxWidth: .infinity)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    HStack(spacing: 8) {
                        PhotosPicker(selection: $photoItem, matching: .images) {
                            Label("Replace", systemImage: "photo.on.rectangle")
                                .font(SimmerlyFont.labelLarge)
                                .foregroundStyle(SimmerlyColor.onSurface)
                                .padding(.horizontal, 16)
                                .frame(height: 40)
                                .overlay(Capsule().strokeBorder(SimmerlyColor.outline, lineWidth: 1))
                        }
                        Button {
                            photoItem = nil
                            pickedPreview = nil
                            onEvent(TimelineEditIntentRemovePhoto.shared)
                        } label: {
                            Label("Remove photo", systemImage: "trash")
                                .font(SimmerlyFont.labelLarge)
                                .foregroundStyle(SimmerlyColor.error)
                                .padding(.horizontal, 12)
                                .frame(height: 40)
                        }
                    }
                }
            } else {
                VStack(spacing: 12) {
                    Image(systemName: "photo.badge.plus")
                        .font(.system(size: 32))
                        .foregroundStyle(SimmerlyColor.outline)
                    Text("Add a photo of how it turned out.")
                        .font(SimmerlyFont.bodyMedium)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    PhotosPicker(selection: $photoItem, matching: .images) {
                        Label("Choose from gallery", systemImage: "photo.on.rectangle")
                            .font(SimmerlyFont.labelLarge)
                            .foregroundStyle(SimmerlyColor.onSurface)
                            .frame(maxWidth: .infinity)
                            .frame(height: 48)
                            .background(SimmerlyColor.surface, in: Capsule())
                            .overlay(Capsule().strokeBorder(SimmerlyColor.outline, lineWidth: 1))
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 24)
                .frame(maxWidth: .infinity)
                .background(SimmerlyColor.surfaceContainerLow, in: RoundedRectangle(cornerRadius: 12))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .strokeBorder(SimmerlyColor.outline, style: StrokeStyle(lineWidth: 1.5, dash: [6, 4]))
                )
            }
        }
    }

    @ViewBuilder
    private var photoPreview: some View {
        if let picked = pickedPreview, state.pickedImage != nil {
            Image(uiImage: picked).resizable().scaledToFill()
        } else if let existing = state.existingImage {
            RemoteImage(url: existing, targetSize: CGSize(width: 358, height: 220)) {
                SimmerlyColor.surfaceContainer
            }
        }
    }

    /// Re-encodes as JPEG so HEIC and other library formats always upload as something Mealie accepts.
    private func loadPhoto(_ item: PhotosPickerItem) async {
        guard let data = try? await item.loadTransferable(type: Data.self),
              let image = UIImage(data: data),
              let jpeg = image.jpegData(compressionQuality: 0.85)
        else { return }
        pickedPreview = image
        onEvent(TimelineEditIntentPhotoPicked(image: IosPickedImage.shared.from(data: jpeg, extension: "jpg")))
    }
}

/// Outlined field with its label sitting on the top border, as in the design's text fields.
private struct OutlinedField<Content: View>: View {
    let label: String
    @ViewBuilder let content: Content

    var body: some View {
        content
            .overlay(
                RoundedRectangle(cornerRadius: 4).strokeBorder(SimmerlyColor.outline, lineWidth: 1)
            )
            .overlay(alignment: .topLeading) {
                Text(label)
                    .font(SimmerlyFont.bodySmall)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                    .padding(.horizontal, 4)
                    .background(SimmerlyColor.background)
                    .padding(.leading, 12)
                    .offset(y: -8)
            }
            .padding(.top, 8)
    }
}
