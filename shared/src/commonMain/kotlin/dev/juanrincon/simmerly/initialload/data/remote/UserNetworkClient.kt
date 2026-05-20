package dev.juanrincon.simmerly.initialload.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.initialload.data.remote.dto.UserRatingsDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.UserDto
import io.ktor.client.HttpClient
import io.ktor.client.request.get

class UserNetworkClient(private val client: HttpClient) {

    suspend fun getSelf(): Either<DataError.NetworkError<Unit>, UserDto> = arrowNetworkHandler {
        client.get("/api/users/self")
    }

    suspend fun getSelfRatings(): Either<DataError.NetworkError<Unit>, UserRatingsDto> =
        arrowNetworkHandler {
            client.get("/api/users/self/ratings")
        }

    suspend fun getSelfFavorites(): Either<DataError.NetworkError<Unit>, UserRatingsDto> =
        arrowNetworkHandler {
            client.get("/api/users/self/favorites")
        }
}
