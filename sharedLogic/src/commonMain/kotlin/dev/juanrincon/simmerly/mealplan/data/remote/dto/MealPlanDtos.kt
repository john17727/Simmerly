package dev.juanrincon.simmerly.mealplan.data.remote.dto

import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
import kotlinx.serialization.Serializable

@Serializable
data class PlanEntryOutDto(
    val id: Int,
    val groupId: String,
    val userId: String,
    val householdId: String? = null,
    val date: String,
    val endDate: String? = null,
    val entryType: String = "breakfast",
    val title: String = "",
    val text: String = "",
    val recipeId: String? = null,
    val recipeScale: Double = 1.0,
    val recipe: RecipeSummaryDto? = null,
)

/** Body of POST (create) and, with [id]/[groupId]/[userId], of PUT (Mealie needs the full body). */
@Serializable
data class PlanEntryInDto(
    val date: String,
    // No defaults on purpose: the shared Json omits default values (encodeDefaults = false), and a
    // PUT must send endDate/recipeId as null to clear them.
    val endDate: String?,
    val entryType: String,
    val title: String,
    val text: String,
    val recipeId: String?,
    val recipeScale: Double,
    val id: Int? = null,
    val groupId: String? = null,
    val userId: String? = null,
)

@Serializable
data class PlanRuleDto(
    val id: String,
    val day: String,
    val entryType: String,
    val queryFilterString: String,
)

@Serializable
data class HouseholdPreferencesDto(val firstDayOfWeek: Int = 0)
