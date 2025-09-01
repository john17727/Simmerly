package dev.juanrincon.simmerly.recipes.data.local.model

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import dev.juanrincon.simmerly.recipes.data.local.entity.CategoryEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.CommentEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.InstructionEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.NoteEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.RecipeEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.TagEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.ToolEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.junction.RecipeCategoryCrossRef
import dev.juanrincon.simmerly.recipes.data.local.entity.junction.RecipeTagCrossRef
import dev.juanrincon.simmerly.recipes.data.local.entity.junction.RecipeToolCrossRef

data class RecipeDetailWithRelations(
    @Embedded
    val recipe: RecipeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(RecipeCategoryCrossRef::class)
    )
    val categories: List<CategoryEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(RecipeTagCrossRef::class)
    )
    val tags: List<TagEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(RecipeToolCrossRef::class)
    )
    val tools: List<ToolEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId",
    )
    val ingredients: List<IngredientWithRelations>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId",
    )
    val instructions: List<InstructionEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId",
    )
    val notes: List<NoteEntity>,

    @Relation(
        parentColumn = "id",
        entityColumn = "recipeId",
    )
    val comments: List<CommentEntity>,
)
