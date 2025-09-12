package dev.juanrincon.simmerly.recipes.domain

sealed interface LoadingResult<out T> {
    data object Loading : LoadingResult<Nothing>
    data class Loaded<T>(val data: T) : LoadingResult<T>
}