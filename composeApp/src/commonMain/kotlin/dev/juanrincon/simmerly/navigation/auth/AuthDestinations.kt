package dev.juanrincon.simmerly.navigation.auth

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
sealed interface AuthDestinations: NavKey {

    @Serializable
    data object Login : AuthDestinations

    @Serializable
    data object App : AuthDestinations

    @Serializable
    data object Splash : AuthDestinations
}