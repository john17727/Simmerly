package dev.juanrincon.simmerly.recipes.presentation.details.models

data class InstructionUi(
    val id: String,
    val title: String?,
    val summary: String,
    val text: String,
    val images: List<String> = emptyList(),
    /**
     * Whether the recipe actually named this step. When it didn't, [summary] holds the generated
     * "Step N" fallback, which Cook Mode wants as a heading but the recipe detail screen must not
     * show — it already draws the number in the step's badge.
     */
    val hasOwnSummary: Boolean = false,
    /**
     * Ids of the ingredients this step uses. They are resolved against the recipe's ingredient list
     * at render time so the quantities stay in sync when the servings are scaled.
     */
    val ingredientIds: List<String> = emptyList()
)
