package dev.juanrincon.simmerly.timeline.data.remote.dto

import kotlinx.serialization.Serializable

/** Body of `PUT /api/recipes/timeline/events/{id}`. Mealie only lets these fields change. */
@Serializable
data class TimelineEventUpdateDto(
    val subject: String,
    val eventMessage: String? = null,
    val image: String? = null,
)

/** `UserSummary` from `GET /api/households/members`; the timeline only carries a `userId`. */
@Serializable
data class MemberDto(
    val id: String,
    val username: String,
    val fullName: String = "",
)
