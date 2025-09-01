package dev.juanrincon.simmerly.recipes.data.local.model

import androidx.room.Embedded
import androidx.room.Relation
import dev.juanrincon.simmerly.recipes.data.local.entity.FoodEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.entity.UnitEntity

data class IngredientWithRelations(
    @Embedded
    val ingredient: IngredientEntity,

    @Relation(
        parentColumn = "unitId",
        entityColumn = "id",
    )
    val unit: UnitEntity?,

    @Relation(
        parentColumn = "foodId",
        entityColumn = "id",
    )
    val food: FoodEntity?
)
