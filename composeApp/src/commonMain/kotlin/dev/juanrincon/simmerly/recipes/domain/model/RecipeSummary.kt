package dev.juanrincon.simmerly.recipes.domain.model

data class RecipeSummary(
    val id: String,
    val name: String,
    val image: String,
    val tags: List<Tag>,
    val rating: Double?,
    val cookTime: String?,
)
