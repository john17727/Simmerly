package dev.juanrincon.simmerly.core.data.remote

import app.tracktion.core.domain.util.DataError
import io.ktor.client.call.body
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.util.network.UnresolvedAddressException
import kotlinx.serialization.SerializationException
import org.mobilenativefoundation.store.store5.FetcherResult

suspend inline fun <reified S : Any, reified E> storeNetworkHandler(
    call: () -> HttpResponse
): FetcherResult<S> {
    val response = try {
        call()
    } catch (e: UnresolvedAddressException) {
        return FetcherResult.Error.Custom(DataError.NetworkError.NoInternet)
    } catch (e: SerializationException) {
        return FetcherResult.Error.Custom(DataError.NetworkError.Serialization)
    } catch (e: SocketTimeoutException) {
        return FetcherResult.Error.Custom(DataError.NetworkError.UnresolvedAddress)
    } catch (e: HttpRequestTimeoutException) {
        return FetcherResult.Error.Custom(DataError.NetworkError.RequestTimeout)
    } catch (e: Exception) {
        // Check for the Java-specific exception by its fully qualified name
        // This relies on the class being available at runtime on the platform
        return FetcherResult.Error.Custom<DataError.NetworkError<E>>(mapPlatformException(e))
    }

    return when (response.status.value) {
        in 200..299 -> {
            FetcherResult.Data(response.body<S>())
        }

        400 -> {
            val errorBody = response.body<E>()
            FetcherResult.Error.Custom(DataError.NetworkError.BadRequest(errorBody))
        }

        401 -> FetcherResult.Error.Custom(DataError.NetworkError.Unauthorized)
        409 -> FetcherResult.Error.Custom(DataError.NetworkError.Conflict)
        408 -> FetcherResult.Error.Custom(DataError.NetworkError.RequestTimeout)
        413 -> FetcherResult.Error.Custom(DataError.NetworkError.PayloadTooLarge)
        in 500..599 -> FetcherResult.Error.Custom(DataError.NetworkError.ServerError)
        else -> FetcherResult.Error.Custom(DataError.NetworkError.Unknown)
    }
}