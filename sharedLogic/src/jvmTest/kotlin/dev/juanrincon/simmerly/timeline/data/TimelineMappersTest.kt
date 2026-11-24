package dev.juanrincon.simmerly.timeline.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isNull
import assertk.assertions.isTrue
import dev.juanrincon.simmerly.core.aUserEntity
import dev.juanrincon.simmerly.recipes.data.remote.dto.RecipeTimelineEventOutDto
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventEntity
import dev.juanrincon.simmerly.timeline.data.local.TimelineEventWithUser
import dev.juanrincon.simmerly.timeline.domain.TimelineEventType
import kotlin.test.Test
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
class TimelineMappersTest {

    private fun dto(timestamp: String, image: String? = null, type: String = "comment") = RecipeTimelineEventOutDto(
        id = "e1", recipeId = "r1", userId = "user-1", subject = "s", eventType = type, eventMessage = "m",
        image = image, timestamp = timestamp, groupId = "g", householdId = "h", createdAt = timestamp, updatedAt = timestamp
    )

    private fun row(user: dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity? = aUserEntity(fullName = "Juan Rincon"),
                    hasImage: Boolean = false, type: String = "comment") =
        TimelineEventWithUser(
            TimelineEventEntity("e1", "r1", "user-1", "s", type, "m", hasImage, Instant.parse("2026-11-21T10:00:00Z")),
            user,
        )

    @Test
    fun aTimestampWithoutAZoneIsReadAsUtc() {
        assertThat(dto("2026-11-21T10:00:00").toEntity().timestamp).isEqualTo(Instant.parse("2026-11-21T10:00:00Z"))
    }

    @Test
    fun aTimestampWithAZoneIsKept() {
        assertThat(dto("2026-11-21T10:00:00+02:00").toEntity().timestamp).isEqualTo(Instant.parse("2026-11-21T08:00:00Z"))
        assertThat(dto("2026-11-21T10:00:00Z").toEntity().timestamp).isEqualTo(Instant.parse("2026-11-21T10:00:00Z"))
    }

    @Test
    fun onlyMealiesHasImageLiteralCountsAsAPhoto() {
        assertThat(dto("2026-11-21T10:00:00", image = "has image").toEntity().hasImage).isTrue()
        assertThat(dto("2026-11-21T10:00:00", image = "does not have image").toEntity().hasImage).isFalse()
        assertThat(dto("2026-11-21T10:00:00", image = null).toEntity().hasImage).isFalse()
    }

    @Test
    fun theAuthorMayEditAndAnAdminMayEditAnyEntry() {
        assertThat(row().toDomain("http://h", "user-1", false).canEdit).isTrue()
        assertThat(row().toDomain("http://h", "someone-else", false).canEdit).isFalse()
        assertThat(row().toDomain("http://h", "someone-else", true).canEdit).isTrue()
        assertThat(row().toDomain("http://h", null, false).canEdit).isFalse()
    }

    @Test
    fun photoUrlsAppearOnlyForEntriesWithAPhoto() {
        val with = row(hasImage = true).toDomain("http://h", "user-1", false)
        val without = row().toDomain("http://h", "user-1", false)

        assertThat(with.image).isEqualTo("http://h/api/media/recipes/r1/images/timeline/e1/original.webp")
        assertThat(with.thumbnail).isEqualTo("http://h/api/media/recipes/r1/images/timeline/e1/min-original.webp")
        assertThat(without.image).isNull()
    }

    @Test
    fun theAuthorNameFallsBackToTheUsernameThenBlank() {
        assertThat(row().toDomain("h", "u", false).authorName).isEqualTo("Juan Rincon")
        assertThat(row(aUserEntity(fullName = "")).toDomain("h", "u", false).authorName).isEqualTo("john")
        assertThat(row(user = null).toDomain("h", "u", false).authorName).isEqualTo("")
    }

    @Test
    fun theAvatarUrlPointsAtTheAuthorsProfileImage() {
        assertThat(row().toDomain("http://h", "u", false).authorImage).isEqualTo("http://h/api/media/users/user-1/profile.webp")
        assertThat(row().toDomain(null, "u", false).authorImage).isEqualTo("")
    }

    @Test
    fun unknownEventTypesAreTreatedAsInfo() {
        assertThat(row(type = "comment").toDomain("h", "u", false).type).isEqualTo(TimelineEventType.Comment)
        assertThat(row(type = "system").toDomain("h", "u", false).type).isEqualTo(TimelineEventType.System)
        assertThat(row(type = "info").toDomain("h", "u", false).type).isEqualTo(TimelineEventType.Info)
        assertThat(row(type = "mystery").toDomain("h", "u", false).type).isEqualTo(TimelineEventType.Info)
    }
}
