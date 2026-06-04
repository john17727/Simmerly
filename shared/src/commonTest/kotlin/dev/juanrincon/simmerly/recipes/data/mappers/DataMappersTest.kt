package dev.juanrincon.simmerly.recipes.data.mappers

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.core.data.remote.dto.ItemListDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.CategoryDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.NutritionDto
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeSummaryDto
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

    // region ItemListDto.toPaginationData

    @Test
    fun itemListDtoToPaginationDataMapsAllFields() {
        val dto = ItemListDto<Unit>(
            page = 2,
            perPage = 50,
            total = 120,
            totalPages = 3,
            next = "?page=3",
            previous = "?page=1",
            items = emptyList()
        )

        val pagination = dto.toPaginationData()

        assertThat(pagination.page).isEqualTo(2)
        assertThat(pagination.perPage).isEqualTo(50)
        assertThat(pagination.total).isEqualTo(120)
        assertThat(pagination.totalPages).isEqualTo(3)
        assertThat(pagination.next).isEqualTo("?page=3")
        assertThat(pagination.previous).isEqualTo("?page=1")
    }

    @Test
    fun itemListDtoToPaginationDataWithNullNextAndPrevious() {
        val dto = ItemListDto<Unit>(
            page = 1, perPage = 50, total = 10, totalPages = 1,
            next = null, previous = null, items = emptyList()
        )

        val pagination = dto.toPaginationData()

        assertThat(pagination.next).isNull()
        assertThat(pagination.previous).isNull()
    }

    // endregion

    // region Helpers

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
