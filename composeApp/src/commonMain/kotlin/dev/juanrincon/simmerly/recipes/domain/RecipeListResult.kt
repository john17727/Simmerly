package dev.juanrincon.simmerly.recipes.domain

import dev.juanrincon.simmerly.recipes.domain.model.PaginationData
import dev.juanrincon.simmerly.recipes.domain.model.RecipeSummary

data class RecipeListResult(
    val items: List<RecipeSummary>,
    val pagination: PaginationData?
)
