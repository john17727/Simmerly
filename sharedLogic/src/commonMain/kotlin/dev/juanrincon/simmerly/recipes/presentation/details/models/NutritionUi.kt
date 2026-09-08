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

/**
 * The nutrition facts that actually have a value, as (label, value) pairs in the order the recipe
 * detail screens render them. Mealie leaves most of these null on an unparsed recipe, so the UI
 * would otherwise repeat the same null check eleven times. Labels live here next to the model for
 * the same reason [dev.juanrincon.simmerly.recipes.presentation.details.orbit.label] does.
 */
val NutritionUi.entries: List<Pair<String, String>>
    get() = listOfNotNull(
        "Calories" pairedWith calories,
        "Carbohydrates" pairedWith carbohydrateContent,
        "Cholesterol" pairedWith cholesterolContent,
        "Fat" pairedWith fatContent,
        "Fiber" pairedWith fiberContent,
        "Protein" pairedWith proteinContent,
        "Saturated Fat" pairedWith saturatedFatContent,
        "Sodium" pairedWith sodiumContent,
        "Sugar" pairedWith sugarContent,
        "Trans Fat" pairedWith transFatContent,
        "Unsaturated Fat" pairedWith unsaturatedFatContent
    )

private infix fun String.pairedWith(value: String?): Pair<String, String>? =
    if (value.isNullOrBlank()) null else this to value
