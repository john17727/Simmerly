package dev.juanrincon.simmerly.core.data.network

import app.tracktion.core.domain.util.DataError
import app.tracktion.core.domain.util.Result
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import kotlin.jvm.javaClass

suspend inline fun <reified S, reified E> networkHandler(
    call: () -> HttpResponse,
): Result<S, DataError.NetworkError<E>> {
    val response = try {
        call()
    } catch (e: UnresolvedAddressException) {
        return Result.Error(DataError.NetworkError.NoInternet)
    } catch (e: SerializationException) {
        return Result.Error(DataError.NetworkError.Serialization)
    } catch (e: Exception) {
        // Check for the Java-specific exception by its fully qualified name
        // This relies on the class being available at runtime on the platform
        if (e::class.simpleName == "UnknownHostException") {
            return Result.Error(DataError.NetworkError.UnresolvedAddress)
        }
        return Result.Error(DataError.NetworkError.Unknown)
    }

    return when (response.status.value) {
        in 200..299 -> {
            Result.Success(response.body<S>())
        }

        400 -> {
            val errorBody = response.body<E>()
            Result.Error(DataError.NetworkError.BadRequest(errorBody))
        }

        401 -> Result.Error(DataError.NetworkError.Unauthorized)
        409 -> Result.Error(DataError.NetworkError.Conflict)
        408 -> Result.Error(DataError.NetworkError.RequestTimeout)
        413 -> Result.Error(DataError.NetworkError.PayloadTooLarge)
        in 500..599 -> Result.Error(DataError.NetworkError.ServerError)
        else -> Result.Error(DataError.NetworkError.Unknown)
    }
}
