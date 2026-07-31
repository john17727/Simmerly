import SwiftUI
import Shared

struct RecipeCommentsView: View {
    let state: RecipeCommentsState
    @Binding var commentText: String
    let onSend: () -> Void

    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                LazyVStack(alignment: .leading, spacing: 0) {
                    ForEach(state.comments, id: \.id) { comment in
                        CommentRow(comment: comment)
                            .padding(.vertical, 12)
                    }
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 16)
            }
            .defaultScrollAnchor(.bottom)

            HStack(spacing: 8) {
                TextField("Join the conversation", text: $commentText, axis: .vertical)
                    .textFieldStyle(.roundedBorder)
                    .lineLimit(1...4)
                Button(action: onSend) {
                    Image(systemName: "paperplane.fill")
                        .foregroundStyle(SimmerlyColor.primary)
                }
                .disabled(commentText.isEmpty)
            }
            .padding(16)
        }
        .navigationTitle("Comments")
        .navigationBarTitleDisplayMode(.inline)
        .background(SimmerlyColor.background)
    }
}

private struct CommentRow: View {
    let comment: CommentUi

    var body: some View {
        HStack(alignment: .top, spacing: 8) {
            RemoteImage(
                url: comment.image,
                targetSize: CGSize(width: 24, height: 24)
            ) {
                Image(systemName: "person.circle.fill")
                    .resizable()
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }
            .frame(width: 24, height: 24)
            .clipShape(RoundedRectangle(cornerRadius: 8))

            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Text(comment.author)
                        .font(SimmerlyFont.bodyLarge)
                    Text(comment.date)
                        .font(SimmerlyFont.bodySmall)
                        .foregroundStyle(SimmerlyColor.onSurfaceVariant)
                }
                Text(comment.text)
                    .font(SimmerlyFont.bodyMedium)
                    .foregroundStyle(SimmerlyColor.onSurfaceVariant)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}
