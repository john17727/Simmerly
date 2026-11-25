package dev.juanrincon.simmerly.core.data.remote

/** The image files Mealie serves for each recipe (its `ImageType` enum). */
enum class RecipeImageSize(val fileName: String) {
    /** The full-size image. For the detail hero. */
    Original("original.webp"),

    /** A reduced image (about 600px wide). Plenty for list/search thumbnails, and far cheaper to
     * download, decode and cache than [Original]. */
    Thumbnail("min-original.webp"),
}

/** The URL of a recipe's image on [host], or an empty string while the host is unknown. */
fun recipeImageUrl(
    host: String?,
    recipeId: String,
    size: RecipeImageSize = RecipeImageSize.Original,
): String {
    if (host == null) return ""
    return "$host/api/media/recipes/$recipeId/images/${size.fileName}"
}

/** The image files Mealie serves for a timeline event photo. */
enum class TimelineImageSize(val fileName: String) {
    Original("original.webp"),
    Thumbnail("min-original.webp"),
}

/** The URL of a timeline event's photo on [host], or an empty string while the host is unknown. */
fun timelineImageUrl(
    host: String?,
    recipeId: String,
    eventId: String,
    size: TimelineImageSize = TimelineImageSize.Original,
): String {
    if (host == null) return ""
    return "$host/api/media/recipes/$recipeId/images/timeline/$eventId/${size.fileName}"
}
