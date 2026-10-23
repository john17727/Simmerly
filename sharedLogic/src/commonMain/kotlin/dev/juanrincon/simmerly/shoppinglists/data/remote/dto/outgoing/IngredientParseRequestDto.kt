package dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

/** Leaves `parser` out, so the server uses its default (`nlp`). */
@Serializable
data class IngredientParseRequestDto(val ingredient: String)
