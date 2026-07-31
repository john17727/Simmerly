import SwiftUI
import Kingfisher

/// Loads a recipe/user image URL, matching the Android side's plain `Coil AsyncImage(model = url)`
/// — Mealie's /api/media endpoints are unauthenticated, so no custom headers are needed here either.
///
/// Uses Kingfisher instead of `AsyncImage`: on device, some recipe-blog hero photos decoded by
/// `AsyncImage` at native resolution corrupted CoreAnimation's compositing of sibling views stacked
/// below the image in a ScrollView. Kingfisher's `.downsampling(size:)` doesn't fix this — it decodes
/// via the same `CGImageSourceCreateThumbnailAtIndex` call `AsyncImage` effectively relies on, at any
/// size. `.resizing(referenceSize:mode:)` does: it redraws the decoded bitmap into a fresh bitmap
/// context (`UIImage.draw(in:)`) before ever handing it to SwiftUI, which is what actually avoids the
/// corruption — a "boring", CPU-rasterized bitmap instead of whatever backing the raw decode returns.
///
/// The crop/fit is applied entirely here via Kingfisher's own `.resizing(mode:)`. Do NOT layer a
/// SwiftUI-level `.aspectRatio(contentMode:)` on top at call sites — stacking one on a `KFImage`
/// reintroduces the same corruption, even though the underlying bitmap is already the clean,
/// redrawn one. Pass `targetSize` matching the view's actual display frame instead.
struct RemoteImage<Placeholder: View, Failure: View>: View {
    let url: String
    @ViewBuilder var placeholder: () -> Placeholder
    @ViewBuilder var failure: () -> Failure

    var body: some View {
        KFImage(URL(string: url))
            .placeholder { placeholder() }
            .onFailureView { failure() }
            .resizable()
            .id(url)
    }
}

extension RemoteImage where Failure == Placeholder {
    init(
        url: String,
        @ViewBuilder placeholder: @escaping () -> Placeholder
    ) {
        self.url = url
        self.placeholder = placeholder
        self.failure = placeholder
    }
}
