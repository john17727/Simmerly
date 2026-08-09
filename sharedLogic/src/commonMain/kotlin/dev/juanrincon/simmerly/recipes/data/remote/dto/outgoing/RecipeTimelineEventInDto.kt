package dev.juanrincon.simmerly.recipes.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

/**
 * Body for `POST /api/recipes/timeline/events`, modelling all seven fields of Mealie's
 * `RecipeTimelineEventIn`. Everything but [recipeId], [subject] and [eventType] is optional per
 * the spec; with `encodeDefaults = false` an unset field is omitted from the wire entirely rather
 * than sent as an explicit `null`.
 *
 * Field notes:
 * - [eventType] is one of Mealie's three literal values (`system`, `info`, `comment`) — not worth
 *   a dedicated enum for the single "comment" value this app ever sends.
 * - [image] is one of Mealie's own literal strings (`"has image"` / `"does not have image"`)
 *   rather than actual image data. Left unset; Mealie's own UI omits it too and the server
 *   applies its default.
 * - [userId] is left unset for the same reason — Mealie's own UI omits it and the server
 *   attributes the event to the requesting user from the auth token, which is strictly more
 *   reliable than trusting a locally cached id.
 * - [timestamp] is *not* left to the server default. Mealie pairs a "made this" timeline entry's
 *   timestamp with the recipe's `lastMade` value, so the caller passes the same instant to both
 *   writes; otherwise the entry would silently record whenever its HTTP call happened to land.
 */
@Serializable
data class RecipeTimelineEventInDto(
    val recipeId: String,
    val userId: String? = null,
    val subject: String,
    val eventType: String,
    val eventMessage: String? = null,
    val image: String? = null,
    val timestamp: String? = null
)
