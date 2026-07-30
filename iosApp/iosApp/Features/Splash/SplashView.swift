import SwiftUI

struct SplashView: View {
    var body: some View {
        ZStack {
            SimmerlyColor.surface.ignoresSafeArea()
            Image("LaunchLogo")
                .renderingMode(.template)
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(minWidth: 100, idealWidth: 125, maxWidth: 150, minHeight: 100, idealHeight: 125, maxHeight: 150)
                .foregroundStyle(SimmerlyColor.primary)
                .accessibilityLabel("Simmerly Logo")
        }
    }
}

#Preview {
    SplashView()
}
