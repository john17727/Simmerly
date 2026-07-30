package dev.juanrincon.simmerly.recipes.presentation.details.models

data class InstructionUi(
    val id: String,
    val title: String?,
    val summary: String,
    val text: String,
    val images: List<String> = emptyList(),
    /**
     * Ids of the ingredients this step uses. They are resolved against the recipe's ingredient list
     * at render time so the quantities stay in sync when the servings are scaled.
     */
    val ingredientIds: List<String> = emptyList()
)
