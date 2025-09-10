package dev.juanrincon.simmerly.recipes.domain

import app.tracktion.core.domain.util.Error

sealed interface RecipesError : Error {
    data object FetchError : RecipesError
}