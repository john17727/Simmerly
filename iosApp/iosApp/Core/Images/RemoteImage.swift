import SwiftUI

/// Loads a recipe/user image URL, matching the Android side's plain `Coil AsyncImage(model = url)`
/// — Mealie's /api/media endpoints are unauthenticated, so no custom headers are needed here either.
struct RemoteImage<Placeholder: View, Failure: View>: View {
    let url: String
    @ViewBuilder var placeholder: () -> Placeholder
    @ViewBuilder var failure: () -> Failure

    var body: some View {
        AsyncImage(url: URL(string: url)) { phase in
            switch phase {
            case .success(let image):
                image.resizable()
            case .failure:
                failure()
            case .empty:
                placeholder()
            @unknown default:
                placeholder()
            }
        }
    }
}

extension RemoteImage where Failure == Placeholder {
    init(url: String, @ViewBuilder placeholder: @escaping () -> Placeholder) {
        self.url = url
        self.placeholder = placeholder
        self.failure = placeholder
    }
}
