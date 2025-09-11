package dev.juanrincon.simmerly.recipes.data.local.recipe.entity.junction

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.InstructionEntity

@Entity(
    tableName = "instruction_ingredient_cross_ref",
    primaryKeys = ["instruction_id", "ingredient_id"],
    foreignKeys = [
        ForeignKey(
            entity = InstructionEntity::class,
            parentColumns = ["id"],
            childColumns = ["instruction_id"],
            onDelete = ForeignKey.CASCADE // If an instruction is deleted, delete its links
        ),
        ForeignKey(
            entity = IngredientEntity::class,
            parentColumns = ["id"],
            childColumns = ["ingredient_id"],
            onDelete = ForeignKey.CASCADE // If an ingredient is deleted, remove it from all instructions
        )
    ]
)
data class InstructionIngredientCrossRef(
    @ColumnInfo(name = "instruction_id", index = true) val instructionId: String,
    @ColumnInfo(name = "ingredient_id", index = true) val ingredientId: String
)