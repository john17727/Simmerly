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
/// redrawn one. Pass `targetSize` matching the view's actual display frame instead: the bitmap
/// arrives already aspect-fill-cropped to those dimensions, so the plain `.resizable()` image fits
/// its frame exactly and can never overflow it. `targetSize: nil` skips processing for layouts
/// whose height derives from the image's own aspect ratio (e.g. instruction step photos).
struct RemoteImage<Placeholder: View, Failure: View>: View {
    let url: String
    var targetSize: CGSize? = nil
    @ViewBuilder var placeholder: () -> Placeholder
    @ViewBuilder var failure: () -> Failure

    @Environment(\.displayScale) private var displayScale

    var body: some View {
        var image = KFImage(URL(string: url))
            .placeholder { placeholder() }
            .onFailureView { failure() }
        if let targetSize, targetSize.width > 0, targetSize.height > 0 {
            image =
                image
                .setProcessor(
                    ResizingImageProcessor(referenceSize: targetSize, mode: .aspectFill)
                        |> CroppingImageProcessor(size: targetSize)
                )
                .scaleFactor(displayScale)
        }
        return image.resizable().id(url)
    }
}

extension RemoteImage where Failure == Placeholder {
    init(
        url: String,
        targetSize: CGSize? = nil,
        @ViewBuilder placeholder: @escaping () -> Placeholder
    ) {
        self.url = url
        self.targetSize = targetSize
        self.placeholder = placeholder
        self.failure = placeholder
    }
}
