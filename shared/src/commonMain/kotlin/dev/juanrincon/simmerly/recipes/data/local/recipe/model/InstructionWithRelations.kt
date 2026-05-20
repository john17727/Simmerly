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
        parentColumns = ["id"],
        entityColumns = ["id"],
        associateBy = Junction(
            InstructionIngredientCrossRef::class,
            parentColumns = ["instruction_id"],
            entityColumns = ["ingredient_id"]
        )
    )
    val ingredients: List<IngredientWithRelations>
)
