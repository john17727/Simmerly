package dev.juanrincon.simmerly.core.data.remote

/** The URL of a recipe's original image on [host], or an empty string while the host is unknown. */
fun recipeImageUrl(host: String?, recipeId: String): String {
    if (host == null) return ""
    return "$host/api/media/recipes/$recipeId/images/original.webp"
}
