package dev.juanrincon.simmerly.timeline.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.Index
import androidx.room3.PrimaryKey
import androidx.room3.Relation
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** No foreign key to `recipes`/`users`: a list refresh clears `recipes`, which must not wipe this. */
@OptIn(ExperimentalTime::class)
@Entity(
    tableName = "timeline_events",
    indices = [Index("recipe_id")],
)
data class TimelineEventEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    @ColumnInfo(name = "user_id") val userId: String,
    val subject: String,
    @ColumnInfo(name = "event_type") val eventType: String,
    @ColumnInfo(name = "event_message") val eventMessage: String?,
    @ColumnInfo(name = "has_image") val hasImage: Boolean,
    val timestamp: Instant,
)

data class TimelineEventWithUser(
    @Embedded val event: TimelineEventEntity,
    @Relation(parentColumns = ["user_id"], entityColumns = ["id"])
    val user: UserEntity?,
)
