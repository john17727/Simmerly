package dev.juanrincon.simmerly.recipes.data.local.recipe.model

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
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
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            RecipeCategoryCrossRef::class,
            parentColumns = ["recipe_id"],
            entityColumns = ["category_id"]
        )
    )
    val categories: List<CategoryEntity>,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            RecipeTagCrossRef::class,
            parentColumns = ["recipe_id"],
            entityColumns = ["tag_id"]
        )
    )
    val tags: List<TagEntity>,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            RecipeToolCrossRef::class,
            parentColumns = ["recipe_id"],
            entityColumns = ["tool_id"]
        )
    )
    val tools: List<ToolEntity>,

    @Relation(
        entity = IngredientEntity::class,
        parentColumns = ["id"],
        entityColumns = ["recipe_id"],
    )
    val ingredients: List<IngredientWithRelations>,

    @Relation(
        entity = InstructionEntity::class,
        parentColumns = ["id"],
        entityColumns = ["recipe_id"],
    )
    val instructions: List<InstructionWithRelations>,

    @Relation(
        parentColumns = ["id"],
        entityColumns = ["recipe_id"],
    )
    val notes: List<NoteEntity>,

    @Relation(
        entity = CommentEntity::class,
        parentColumns = ["id"],
        entityColumns = ["recipe_id"],
    )
    val comments: List<CommentWithRelations>,
)
