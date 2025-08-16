package dev.juanrincon.simmerly.core.domain.network

interface BaseUrlProvider {
    fun current(): String? // quick getter for the latest value, or null
}