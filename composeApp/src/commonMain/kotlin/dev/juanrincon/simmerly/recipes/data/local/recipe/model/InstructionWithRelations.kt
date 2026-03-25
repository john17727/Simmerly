package dev.juanrincon.simmerly.recipes.data.local.recipe.model

import androidx.room3.Embedded
import androidx.room3.Junction
import androidx.room3.Relation
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.InstructionEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction.InstructionIngredientCrossRef

data class InstructionWithRelations(
    @Embedded
    val instruction: InstructionEntity,

    @Relation(
        entity = IngredientEntity::class,
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            InstructionIngredientCrossRef::class,
            parentColumn = "instruction_id",
            entityColumn = "ingredient_id"
        )
    )
    val ingredients: List<IngredientWithRelations>
)
