package dev.juanrincon.simmerly.recipes.data.mappers

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.recipes.data.remote.dto.CategoryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.IngredientDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.InstructionDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.NutritionDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.ReferenceDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.SettingsDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.TagDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.ToolDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.UserDto
import kotlin.test.Test

class DataMappersTest {

    // region RecipeSummaryDto.toEntity

    @Test
    fun recipeSummaryDtoToEntityMapsBasicFields() {
        val dto = aRecipeSummaryDto(id = "recipe-1", name = "Pasta")

        val entity = dto.toEntity()

        assertThat(entity.id).isEqualTo("recipe-1")
        assertThat(entity.name).isEqualTo("Pasta")
        assertThat(entity.slug).isEqualTo("pasta")
        assertThat(entity.rating).isEqualTo(4.5)
    }

    @Test
    fun recipeSummaryDtoToEntityMapsNullableFields() {
        val dto = aRecipeSummaryDto(prepTime = null, lastMade = null)

        val entity = dto.toEntity()

        assertThat(entity.prepTime).isNull()
        assertThat(entity.lastMade).isNull()
    }

    @Test
    fun recipeSummaryDtoToEntityUsesEmptyNutritionAndSettings() {
        val dto = aRecipeSummaryDto()

        val entity = dto.toEntity()

        assertThat(entity.nutrition.calories).isNull()
        assertThat(entity.settings.public).isNull()
    }

    // endregion

    // region NutritionDto.toEntity

    @Test
    fun nutritionDtoToEntityMapsAllFields() {
        val dto = NutritionDto(
            calories = "300",
            carbohydrateContent = "40g",
            cholesterolContent = "15mg",
            fatContent = "10g",
            fiberContent = "6g",
            proteinContent = "20g",
            saturatedFatContent = "2g",
            sodiumContent = "500mg",
            sugarContent = "8g",
            transFatContent = "0g",
            unsaturatedFatContent = "8g"
        )

        val entity = dto.toEntity()

        assertThat(entity.calories).isEqualTo("300")
        assertThat(entity.carbohydrates).isEqualTo("40g")
        assertThat(entity.cholesterol).isEqualTo("15mg")
        assertThat(entity.fat).isEqualTo("10g")
        assertThat(entity.protein).isEqualTo("20g")
    }

    @Test
    fun nutritionDtoToEntityWithAllNullsPreservesNulls() {
        val dto = NutritionDto(
            calories = null, carbohydrateContent = null, cholesterolContent = null,
            fatContent = null, fiberContent = null, proteinContent = null,
            saturatedFatContent = null, sodiumContent = null, sugarContent = null,
            transFatContent = null, unsaturatedFatContent = null
        )

        val entity = dto.toEntity()

        assertThat(entity.calories).isNull()
        assertThat(entity.protein).isNull()
    }

    // endregion

    // region SettingsDto.toEntity

    @Test
    fun settingsDtoToEntityMapsAllFields() {
        val dto = SettingsDto(
            public = false,
            showNutrition = true,
            showAssets = true,
            landscapeView = false,
            disableComments = true,
            locked = false
        )

        val entity = dto.toEntity()

        assertThat(entity.public).isEqualTo(false)
        assertThat(entity.showNutrition).isEqualTo(true)
        assertThat(entity.showAssets).isEqualTo(true)
        assertThat(entity.disableComments).isEqualTo(true)
        assertThat(entity.locked).isEqualTo(false)
    }

    // endregion

    // region CategoryDto.toEntity / TagDto.toEntity / ToolDto.toEntity / UserDto.toEntity

    @Test
    fun categoryDtoToEntityMapsAllFields() {
        val dto = CategoryDto(id = "cat-1", groupId = "group-1", name = "Italian", slug = "italian")

        val entity = dto.toEntity()

        assertThat(entity.id).isEqualTo("cat-1")
        assertThat(entity.groupId).isEqualTo("group-1")
        assertThat(entity.name).isEqualTo("Italian")
        assertThat(entity.slug).isEqualTo("italian")
    }

    @Test
    fun tagDtoToEntityMapsAllFields() {
        val dto = TagDto(id = "tag-1", groupId = "group-1", name = "Vegan", slug = "vegan")

        val entity = dto.toEntity()

        assertThat(entity.id).isEqualTo("tag-1")
        assertThat(entity.name).isEqualTo("Vegan")
    }

    @Test
    fun toolDtoToEntityMapsAllFields() {
        val dto = ToolDto(id = "tool-1", groupId = "group-1", name = "Blender", slug = "blender")

        val entity = dto.toEntity()

        assertThat(entity.id).isEqualTo("tool-1")
        assertThat(entity.name).isEqualTo("Blender")
    }

    @Test
    fun userDtoToEntityMapsAllFields() {
        val dto = UserDto(id = "user-1", username = "john", admin = true, fullName = "John Doe")

        val entity = dto.toEntity()

        assertThat(entity.id).isEqualTo("user-1")
        assertThat(entity.username).isEqualTo("john")
        assertThat(entity.admin).isEqualTo(true)
        assertThat(entity.fullName).isEqualTo("John Doe")
    }

    // endregion

    // region InstructionDto.toEntityWithRelations

    @Test
    fun instructionDtoToEntityWithRelationsResolvesIngredientReferences() {
        val ingredients =
            listOf(anIngredient("ref-1"), anIngredient("ref-2"), anIngredient("ref-3"))
        val dto = anInstructionDto(referenceIds = listOf("ref-3", "ref-1"))

        val entity = dto.toEntityWithRelations(recipeId = "recipe-1", ingredients = ingredients)

        assertThat(entity.instruction.recipeId).isEqualTo("recipe-1")
        assertThat(entity.ingredients.map { it.ingredient.id }).isEqualTo(listOf("ref-3", "ref-1"))
    }

    @Test
    fun instructionDtoToEntityWithRelationsSkipsUnknownIngredientReferences() {
        val ingredients = listOf(anIngredient("ref-1"))
        val dto = anInstructionDto(referenceIds = listOf("ref-1", "deleted-ref"))

        val entity = dto.toEntityWithRelations(recipeId = "recipe-1", ingredients = ingredients)

        assertThat(entity.ingredients.map { it.ingredient.id }).isEqualTo(listOf("ref-1"))
    }

    @Test
    fun instructionDtoToEntityWithRelationsWithNoReferencesMapsNoIngredients() {
        val dto = anInstructionDto(referenceIds = emptyList())

        val entity = dto.toEntityWithRelations(recipeId = "recipe-1", ingredients = emptyList())

        assertThat(entity.ingredients).isEmpty()
    }

    // endregion

    // region Helpers

    private fun anInstructionDto(
        referenceIds: List<String>,
        id: String = "instruction-1"
    ) = InstructionDto(
        id = id,
        title = "",
        summary = "Sautee Onions",
        text = "Saute the onion.",
        ingredientReferences = referenceIds.map { ReferenceDto(referenceId = it) }
    )

    private fun anIngredient(referenceId: String) = IngredientDto(
        quantity = 1.0,
        unit = null,
        food = null,
        note = null,
        display = "1 onion",
        title = null,
        originalText = "1 onion",
        referenceId = referenceId
    ).toEntityWithRelations("recipe-1")

    private fun aRecipeSummaryDto(
        id: String = "recipe-1",
        name: String = "Test Recipe",
        prepTime: String? = "10 minutes",
        lastMade: String? = null
    ) = RecipeSummaryDto(
        id = id,
        userId = "user-1",
        householdId = "household-1",
        groupId = "group-1",
        name = name,
        slug = name.lowercase(),
        image = "",
        recipeServings = 4.0,
        recipeYieldQuantity = 4.0,
        recipeYield = "",
        totalTime = "45 minutes",
        prepTime = prepTime,
        cookTime = null,
        performTime = null,
        description = "A test recipe",
        recipeCategory = emptyList(),
        tags = emptyList(),
        tools = emptyList(),
        rating = 4.5,
        orgURL = "",
        dateAdded = "2024-01-15",
        dateUpdated = "2024-06-01T12:00:00Z",
        createdAt = "2024-06-01T12:00:00Z",
        updatedAt = "2024-06-01T12:00:00Z",
        lastMade = lastMade
    )

    // endregion
}
