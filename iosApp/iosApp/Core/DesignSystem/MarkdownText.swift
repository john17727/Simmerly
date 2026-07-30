import SwiftUI

/// Renders recipe instruction markdown, replacing compose-rich-editor's `RichText`. Images are
/// already stripped out of the markdown server-side in DomainMappers.kt and rendered separately
/// via `RemoteImage`, so this only needs inline formatting (bold/italic/links/lists).
struct MarkdownText: View {
    let markdown: String

    var body: some View {
        if let attributed = try? AttributedString(
            markdown: markdown,
            options: AttributedString.MarkdownParsingOptions(interpretedSyntax: .inlineOnlyPreservingWhitespace)
        ) {
            Text(attributed)
        } else {
            Text(markdown)
        }
    }
}
