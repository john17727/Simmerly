package dev.juanrincon.simmerly.recipes.data.local.recipe.model

import androidx.room3.Embedded
import androidx.room3.Relation
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.FoodEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.IngredientEntity
import dev.juanrincon.simmerly.recipes.data.local.recipe.entity.UnitEntity

data class IngredientWithRelations(
    @Embedded
    val ingredient: IngredientEntity,

    @Relation(
        parentColumn = "unit_id",
        entityColumn = "id",
    )
    val unit: UnitEntity?,

    @Relation(
        parentColumn = "food_id",
        entityColumn = "id",
    )
    val food: FoodEntity?
)
