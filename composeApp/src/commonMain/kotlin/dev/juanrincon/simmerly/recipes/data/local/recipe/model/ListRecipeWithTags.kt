package dev.juanrincon.simmerly.recipes.data.local.recipe.model

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef

data class ListRecipeWithTags(
    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeTagCrossRef::class,
            parentColumn = "recipe_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>,
)
