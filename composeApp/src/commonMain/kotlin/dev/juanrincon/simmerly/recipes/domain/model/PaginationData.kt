package dev.juanrincon.simmerly.recipes.domain.model

data class PaginationData(
    val page: Int,
    val perPage: Int,
    val total: Int,
    val totalPages: Int,
    val next: Int?,
    val previous: Int?
)
