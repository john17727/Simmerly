package dev.juanrincon.simmerly.shoppinglists.data.remote.dto.outgoing

import kotlinx.serialization.Serializable

@Serializable
data class NewShoppingListDto(val name: String)
