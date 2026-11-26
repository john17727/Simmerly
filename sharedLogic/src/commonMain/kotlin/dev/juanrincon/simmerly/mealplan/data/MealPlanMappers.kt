package dev.juanrincon.simmerly.mealplan.data

import dev.juanrincon.simmerly.core.data.remote.RecipeImageSize
import dev.juanrincon.simmerly.core.data.remote.recipeImageUrl
import dev.juanrincon.simmerly.mealplan.data.local.MealPlanEntryEntity
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanEntryInDto
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanEntryOutDto
import dev.juanrincon.simmerly.mealplan.data.remote.dto.PlanRuleDto
import dev.juanrincon.simmerly.mealplan.domain.MealPlanDraft
import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.mealplan.domain.MealType
import dev.juanrincon.simmerly.mealplan.domain.PlanRule
import dev.juanrincon.simmerly.mealplan.domain.PlannedRecipe
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

internal fun PlanEntryOutDto.toEntity() = MealPlanEntryEntity(
    id = id, groupId = groupId, userId = userId, date = date, endDate = endDate,
    entryType = entryType, title = title, text = text, recipeId = recipeId ?: recipe?.id,
    recipeScale = recipeScale, recipeName = recipe?.name, recipeServings = recipe?.recipeServings,
)

internal fun MealPlanEntryEntity.toDomain(host: String?) = MealPlanEntry(
    id = id, groupId = groupId, userId = userId,
    date = LocalDate.parse(date), endDate = endDate?.let(LocalDate::parse),
    type = MealType.fromApi(entryType), title = title, text = text,
    recipe = recipeId?.let {
        PlannedRecipe(
            id = it,
            name = recipeName.orEmpty(),
            servings = recipeServings ?: 1.0,
            image = recipeImageUrl(host, it, RecipeImageSize.Thumbnail),
        )
    },
    recipeScale = recipeScale,
)

internal fun MealPlanDraft.toDto() = PlanEntryInDto(
    date = date.toString(), endDate = endDate?.toString(), entryType = type.apiValue,
    title = title, text = text, recipeId = recipeId, recipeScale = recipeScale,
)

internal fun MealPlanEntry.toUpdateDto() = PlanEntryInDto(
    date = date.toString(), endDate = endDate?.toString(), entryType = type.apiValue,
    title = title, text = text, recipeId = recipe?.id, recipeScale = recipeScale,
    id = id, groupId = groupId, userId = userId,
)

internal fun PlanRuleDto.toDomain() = PlanRule(
    day = when (day) {
        "monday" -> DayOfWeek.MONDAY
        "tuesday" -> DayOfWeek.TUESDAY
        "wednesday" -> DayOfWeek.WEDNESDAY
        "thursday" -> DayOfWeek.THURSDAY
        "friday" -> DayOfWeek.FRIDAY
        "saturday" -> DayOfWeek.SATURDAY
        "sunday" -> DayOfWeek.SUNDAY
        else -> null
    },
    type = MealType.fromApi(entryType),
    queryFilter = queryFilterString,
)

/** Mealie counts days from Sunday = 0 (to be confirmed against the live server). */
internal fun Int.toDayOfWeek(): DayOfWeek = if (this == 0 || this == 7) DayOfWeek.SUNDAY else DayOfWeek(this)
