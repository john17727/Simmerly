import SwiftUI

/// Decorative image collage ported from welcome/presentation/WelcomeCollage.kt (compact layout only).
struct WelcomeCollage: View {
    private let columns = 5
    private let gap: CGFloat = 8

    var body: some View {
        GeometryReader { geometry in
            let cellSize = (geometry.size.width - gap * CGFloat(columns - 1)) / CGFloat(columns)
            HStack(spacing: gap) {
                VStack(spacing: gap) {
                    RecipeSquareImage("recipe_1", size: cellSize)
                    RecipeSquareImage("recipe_2", size: cellSize)
                    ColorBlock(SimmerlyColor.tertiary, size: cellSize)
                }
                VStack(spacing: gap) {
                    ColorBlock(SimmerlyColor.secondaryContainer, size: cellSize)
                    LogoCell(size: cellSize)
                    RecipeCircleImage("recipe_3", size: cellSize)
                }
                VStack(spacing: gap) {
                    RecipeSquareImage("recipe_5", size: cellSize)
                    ColorBlock(SimmerlyColor.primary, size: cellSize)
                    RecipeSquareImage("recipe_6", size: cellSize)
                }
                VStack(spacing: gap) {
                    RecipeSquareImage("recipe_7", size: cellSize)
                    RecipeCircleImage("recipe_8", size: cellSize)
                    RecipeSquareImage("recipe_9", size: cellSize)
                }
                VStack(spacing: gap) {
                    RecipeSquareImage("recipe_10", size: cellSize)
                    RecipeSquareImage("recipe_11", size: cellSize)
                    ColorBlock(SimmerlyColor.tertiaryContainer, size: cellSize)
                }
            }
        }
    }
}

private struct RecipeSquareImage: View {
    let name: String
    let size: CGFloat

    init(_ name: String, size: CGFloat) {
        self.name = name
        self.size = size
    }

    var body: some View {
        Image(name)
            .resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: size, height: size)
            .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

private struct RecipeCircleImage: View {
    let name: String
    let size: CGFloat

    init(_ name: String, size: CGFloat) {
        self.name = name
        self.size = size
    }

    var body: some View {
        Image(name)
            .resizable()
            .aspectRatio(contentMode: .fill)
            .frame(width: size, height: size)
            .clipShape(Circle())
    }
}

private struct ColorBlock: View {
    let color: Color
    let size: CGFloat

    init(_ color: Color, size: CGFloat) {
        self.color = color
        self.size = size
    }

    var body: some View {
        RoundedRectangle(cornerRadius: 12)
            .fill(color)
            .frame(width: size, height: size)
    }
}

private struct LogoCell: View {
    let size: CGFloat

    var body: some View {
        Image("LaunchLogo")
            .renderingMode(.template)
            .resizable()
            .aspectRatio(contentMode: .fit)
            .frame(width: size * 0.6, height: size * 0.6)
            .foregroundStyle(SimmerlyColor.primary)
            .frame(width: size, height: size)
            .accessibilityLabel("Simmerly")
    }
}

#Preview {
    WelcomeCollage()
        .frame(height: 200)
}
