package dev.juanrincon.simmerly.recipes.data.mappers

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NutritionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.SettingsEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.ToolEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.CommentWithRelations
import dev.juanrincon.simmerly.recipes.data.local.recipe.model.ListRecipeWithTags
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class DomainMappersTest {

    // region RecipeEntity.toDomain

    @Test
    fun recipeEntityToDomainMapsAllBasicFields() {
        val now = Clock.System.now()
        val entity = aRecipeEntity(now = now)

        val domain = entity.toDomain(host = "https://myserver.com")

        assertThat(domain.id).isEqualTo("recipe-1")
        assertThat(domain.name).isEqualTo("Test Recipe")
        assertThat(domain.rating).isEqualTo(4.5)
        assertThat(domain.description).isEqualTo("A delicious test recipe")
    }

    @Test
    fun recipeEntityToDomainBuildsImageUrlFromHostAndId() {
        val entity = aRecipeEntity()

        val domain = entity.toDomain(host = "https://myserver.com")

        assertThat(domain.image).isEqualTo("https://myserver.com/api/media/recipes/recipe-1/images/original.webp")
    }

    @Test
    fun recipeEntityToDomainWithNullHostProducesEmptyImageUrl() {
        val entity = aRecipeEntity()

        val domain = entity.toDomain(host = null)

        assertThat(domain.image).isEqualTo("")
    }

    @Test
    fun recipeEntityToDomainAbbreviatesHourInTotalTime() {
        val entity = aRecipeEntity(totalTime = "1 hour 30 minutes")

        val domain = entity.toDomain(host = null)

        assertThat(domain.totalTime).isEqualTo("1 h 30 minutes")
    }

    @Test
    fun recipeEntityToDomainAbbreviatesMinutesInPrepTime() {
        val entity = aRecipeEntity(prepTime = "20 minutes")

        val domain = entity.toDomain(host = null)

        assertThat(domain.prepTime).isEqualTo("20 min")
    }

    @Test
    fun recipeEntityToDomainWithNullPrepTimeMapsToNull() {
        val entity = aRecipeEntity(prepTime = null)

        val domain = entity.toDomain(host = null)

        assertThat(domain.prepTime).isNull()
    }

    // endregion

    // region ListRecipeWithTags.toDomain

    @Test
    fun listRecipeWithTagsToDomainIncludesTags() {
        val tags = listOf(
            TagEntity(id = "tag-1", groupId = "group-1", name = "Vegetarian", slug = "vegetarian"),
            TagEntity(id = "tag-2", groupId = "group-1", name = "Quick", slug = "quick")
        )
        val withTags = ListRecipeWithTags(recipe = aRecipeEntity(), tags = tags)

        val domain = withTags.toDomain(host = null)

        assertThat(domain.tags.size).isEqualTo(2)
        assertThat(domain.tags[0].name).isEqualTo("Vegetarian")
        assertThat(domain.tags[1].name).isEqualTo("Quick")
    }

    @Test
    fun listRecipeWithTagsToDomainWithIsFavoriteSetToTrue() {
        val withTags = ListRecipeWithTags(recipe = aRecipeEntity(), tags = emptyList())

        val domain = withTags.toDomain(host = null, isFavorite = true)

        assertThat(domain.isFavorite).isEqualTo(true)
    }

    // endregion

    // region NutritionEntity.toDomain

    @Test
    fun nutritionEntityToDomainMapsAllFields() {
        val entity = NutritionEntity(
            calories = "250",
            carbohydrates = "30g",
            cholesterol = "10mg",
            fat = "12g",
            fiber = "5g",
            protein = "8g",
            saturatedFat = "3g",
            sodium = "200mg",
            sugar = "4g",
            transFat = "0g",
            unsaturatedFat = "9g"
        )

        val domain = entity.toDomain()

        assertThat(domain.calories).isEqualTo("250")
        assertThat(domain.carbohydrateContent).isEqualTo("30g")
        assertThat(domain.proteinContent).isEqualTo("8g")
    }

    @Test
    fun nutritionEntityToDomainWithAllNullsProducesAllNulls() {
        val domain = NutritionEntity.empty().toDomain()

        assertThat(domain.calories).isNull()
        assertThat(domain.fatContent).isNull()
    }

    // endregion

    // region SettingsEntity.toDomain

    @Test
    fun settingsEntityToDomainWithNullValuesFallsBackToDefaults() {
        val entity = SettingsEntity.empty()

        val domain = entity.toDomain()

        assertThat(domain.public).isEqualTo(true)
        assertThat(domain.showNutrition).isEqualTo(false)
        assertThat(domain.showAssets).isEqualTo(false)
        assertThat(domain.landscapeView).isEqualTo(false)
        assertThat(domain.disableComments).isEqualTo(false)
        assertThat(domain.locked).isEqualTo(false)
    }

    @Test
    fun settingsEntityToDomainMapsExplicitValues() {
        val entity = SettingsEntity(
            public = false,
            showNutrition = true,
            showAssets = true,
            landscapeView = true,
            disableComments = true,
            locked = true
        )

        val domain = entity.toDomain()

        assertThat(domain.public).isEqualTo(false)
        assertThat(domain.showNutrition).isEqualTo(true)
        assertThat(domain.disableComments).isEqualTo(true)
    }

    // endregion

    // region Category/Tag/Tool entity.toDomain

    @Test
    fun categoryEntityToDomainMapsAllFields() {
        val entity = CategoryEntity(id = "cat-1", groupId = "group-1", name = "Italian", slug = "italian")

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo("cat-1")
        assertThat(domain.groupId).isEqualTo("group-1")
        assertThat(domain.name).isEqualTo("Italian")
    }

    @Test
    fun tagEntityToDomainMapsAllFields() {
        val entity = TagEntity(id = "tag-1", groupId = "group-1", name = "Vegan", slug = "vegan")

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo("tag-1")
        assertThat(domain.name).isEqualTo("Vegan")
    }

    @Test
    fun toolEntityToDomainMapsAllFields() {
        val entity = ToolEntity(id = "tool-1", groupId = "group-1", name = "Blender", slug = "blender")

        val domain = entity.toDomain()

        assertThat(domain.id).isEqualTo("tool-1")
        assertThat(domain.name).isEqualTo("Blender")
    }

    // endregion

    // region UserEntity.toDomain

    @Test
    fun userEntityToDomainBuildsImageUrlFromHostAndId() {
        val entity = UserEntity(id = "user-1", username = "john", admin = false, fullName = "John Doe")

        val domain = entity.toDomain(host = "https://myserver.com")

        assertThat(domain.image).isEqualTo("https://myserver.com/api/media/users/user-1/profile.webp")
        assertThat(domain.fullName).isEqualTo("John Doe")
    }

    @Test
    fun userEntityToDomainWithNullHostProducesEmptyImageUrl() {
        val entity = UserEntity(id = "user-1", username = "john", admin = false, fullName = "John Doe")

        val domain = entity.toDomain(host = null)

        assertThat(domain.image).isEqualTo("")
    }

    // endregion

    // region CommentWithRelations.toDomain

    @Test
    fun commentWithRelationsToDomainMapsTextAndId() {
        val now = Clock.System.now()
        val entity = CommentWithRelations(
            comment = CommentEntity(
                id = "comment-1",
                recipeId = "recipe-1",
                text = "Delicious!",
                createdAt = now,
                updatedAt = now,
                userId = "user-1"
            ),
            user = UserEntity(id = "user-1", username = "john", admin = false, fullName = "John Doe")
        )

        val domain = entity.toDomain(host = null)

        assertThat(domain.id).isEqualTo("comment-1")
        assertThat(domain.text).isEqualTo("Delicious!")
        assertThat(domain.user.fullName).isEqualTo("John Doe")
    }

    // endregion

    // region Helpers

    private fun aRecipeEntity(
        id: String = "recipe-1",
        name: String = "Test Recipe",
        totalTime: String = "45 minutes",
        prepTime: String? = "10 minutes",
        rating: Double? = 4.5,
        now: kotlin.time.Instant = Clock.System.now()
    ) = RecipeEntity(
        id = id,
        userId = "user-1",
        householdId = "household-1",
        groupId = "group-1",
        name = name,
        slug = "test-recipe",
        image = "",
        servings = 4.0,
        yieldQuantity = 4.0,
        yield = "",
        totalTime = totalTime,
        prepTime = prepTime,
        cookTime = null,
        performTime = null,
        description = "A delicious test recipe",
        rating = rating,
        originalUrl = "",
        dateAdded = now,
        dateUpdated = now,
        createdAt = now,
        updatedAt = now,
        lastMade = null,
        nutrition = NutritionEntity.empty(),
        settings = SettingsEntity.empty()
    )

    // endregion
}
