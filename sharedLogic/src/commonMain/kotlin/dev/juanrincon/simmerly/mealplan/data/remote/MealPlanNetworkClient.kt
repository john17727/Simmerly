package dev.juanrincon.simmerly.mealplan.data.remote

import app.tracktion.core.domain.util.DataError
import arrow.core.Either
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandler
import dev.juanrincon.simmerly.core.data.remote.arrowNetworkHandlerNoContent
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.mealplan.data.remote.dto.HouseholdPreferencesDto
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanEntryInDto
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanEntryOutDto
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanRuleDto
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import kotlinx.datetime.LocalDate

class MealPlanNetworkClient(private val client: HttpClient) {

    /** Entries overlapping [start]..[end]; [includeWeekEntries] also returns whole-week entries. */
    suspend fun getEntries(
        start: LocalDate,
        end: LocalDate,
    ): Either<DataError.NetworkError<Unit>, ItemListDto<PlanEntryOutDto>> = arrowNetworkHandler {
        client.get("/api/households/mealplans") {
            parameter("start_date", start.toString())
            parameter("end_date", end.toString())
            parameter("include_week_entries", true)
            parameter("page", 1)
            parameter("perPage", -1)
            parameter("orderBy", "date")
            parameter("orderDirection", "asc")
        }
    }

    suspend fun create(body: PlanEntryInDto): Either<DataError.NetworkError<Unit>, PlanEntryOutDto> =
        arrowNetworkHandler { client.post("/api/households/mealplans") { setBody(body) } }

    suspend fun update(id: Int, body: PlanEntryInDto): Either<DataError.NetworkError<Unit>, PlanEntryOutDto> =
        arrowNetworkHandler { client.put("/api/households/mealplans/$id") { setBody(body) } }

    suspend fun delete(id: Int): Either<DataError.NetworkError<Unit>, Unit> =
        arrowNetworkHandlerNoContent { client.delete("/api/households/mealplans/$id") }

    suspend fun getRules(): Either<DataError.NetworkError<Unit>, ItemListDto<PlanRuleDto>> =
        arrowNetworkHandler {
            client.get("/api/households/mealplans/rules") {
                parameter("page", 1)
                parameter("perPage", -1)
            }
        }

    suspend fun getPreferences(): Either<DataError.NetworkError<Unit>, HouseholdPreferencesDto> =
        arrowNetworkHandler { client.get("/api/households/preferences") }
}
