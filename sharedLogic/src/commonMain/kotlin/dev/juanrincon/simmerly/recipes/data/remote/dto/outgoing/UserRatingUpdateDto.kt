package dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

/** Body for `POST /api/users/{id}/ratings/{slug}`. [isFavorite] is left `null` when only the
 * rating is being changed — `encodeDefaults = false` on the shared `Json` config then omits it
 * from the wire entirely, so a rating write never clobbers the favorite flag. */
@Serializable
data class UserRatingUpdateDto(
    val rating: Double?,
    val isFavorite: Boolean? = null
)
