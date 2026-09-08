package dev.juanrincon.simmerly.recipes.presentation.details.models

data class NutritionUi(
    val calories: String?,
    val carbohydrateContent: String?,
    val cholesterolContent: String?,
    val fatContent: String?,
    val fiberContent: String?,
    val proteinContent: String?,
    val saturatedFatContent: String?,
    val sodiumContent: String?,
    val sugarContent: String?,
    val transFatContent: String?,
    val unsaturatedFatContent: String?
)

/** One nutrition fact with a value, ready to render. */
data class NutritionEntryUi(val label: String, val value: String)

/**
 * The nutrition facts that actually have a value, in the order the recipe detail screens render
 * them. Mealie leaves most of these null on an unparsed recipe, so every UI would otherwise repeat
 * the same eleven null checks — as the Compose and SwiftUI detail screens both used to. Labels live
 * here next to the model for the same reason
 * [dev.juanrincon.simmerly.recipes.presentation.details.orbit.label] does.
 */
val NutritionUi.entries: List<NutritionEntryUi>
    get() = listOfNotNull(
        "Calories" entryFor calories,
        "Carbohydrates" entryFor carbohydrateContent,
        "Cholesterol" entryFor cholesterolContent,
        "Fat" entryFor fatContent,
        "Fiber" entryFor fiberContent,
        "Protein" entryFor proteinContent,
        "Saturated Fat" entryFor saturatedFatContent,
        "Sodium" entryFor sodiumContent,
        "Sugar" entryFor sugarContent,
        "Trans Fat" entryFor transFatContent,
        "Unsaturated Fat" entryFor unsaturatedFatContent
    )

private infix fun String.entryFor(value: String?): NutritionEntryUi? =
    if (value.isNullOrBlank()) null else NutritionEntryUi(this, value)
