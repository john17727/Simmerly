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
        parentColumns = ["unit_id"],
        entityColumns = ["id"],
    )
    val unit: UnitEntity?,

    @Relation(
        parentColumns = ["food_id"],
        entityColumns = ["id"],
    )
    val food: FoodEntity?
)
