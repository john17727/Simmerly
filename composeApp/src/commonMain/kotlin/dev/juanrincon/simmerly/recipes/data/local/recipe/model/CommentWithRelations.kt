package dev.juanrincon.simmerly.recipes.data.local.recipe.model

import androidx.room.Embedded
import androidx.room.Relation
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UserEntity

data class CommentWithRelations(
    @Embedded
    val comment: CommentEntity,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "id"
    )
    val user: UserEntity
)
