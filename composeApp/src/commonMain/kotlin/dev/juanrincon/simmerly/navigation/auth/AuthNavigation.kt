package dev.juanrincon.simmerly.navigation.auth

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface Route: NavKey

@Serializable
data object Login : Route

@Serializable
data object App : Route

@Serializable
data object Splash : Route