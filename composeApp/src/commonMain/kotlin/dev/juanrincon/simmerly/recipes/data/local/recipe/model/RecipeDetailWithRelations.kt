package dev.juanrincon.simmerly.recipes.data.local.recipe.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.InstructionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.NoteEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.ToolEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeCategoryCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.RecipeToolCrossRef

data class RecipeDetailWithRelations(
    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeCategoryCrossRef::class,
            parentColumn = "recipe_id",
            entityColumn = "category_id"
        )
    )
    val categories: List<CategoryEntity>,

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

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            RecipeToolCrossRef::class,
            parentColumn = "recipe_id",
            entityColumn = "tool_id"
        )
    )
    val tools: List<ToolEntity>,

    @Relation(
        entity = IngredientEntity::class,
        parentColumn = "id",
        entityColumn = "recipe_id",
    )
    val ingredients: List<IngredientWithRelations>,

    @Relation(
        entity = InstructionEntity::class,
        parentColumn = "id",
        entityColumn = "recipe_id",
    )
    val instructions: List<InstructionWithRelations>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipe_id",
    )
    val notes: List<NoteEntity>,

    @Relation(
        entity = CommentEntity::class,
        parentColumn = "id",
        entityColumn = "recipe_id",
    )
    val comments: List<CommentWithRelations>,
)
