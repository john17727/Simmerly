package dev.juanrincon.simmerly.shoppinglists.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Embedded
import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import androidx.room3.Relation

@Entity(tableName = "shopping_lists")
data class ShoppingListEntity(
    @PrimaryKey val id: String,
    val name: String,
    @ColumnInfo(name = "user_id") val userId: String,
    /** JSON array of label ids, in the order the list sorts its aisles. */
    @ColumnInfo(name = "label_order") val labelOrder: String,
)

@Entity(
    tableName = "shopping_list_recipes",
    primaryKeys = ["list_id", "recipe_id"],
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class ShoppingListRecipeEntity(
    @ColumnInfo(name = "list_id", index = true) val listId: String,
    @ColumnInfo(name = "recipe_id") val recipeId: String,
    /** The recipe's index in the list's own order, which the server doesn't otherwise expose. */
    val position: Int,
    val name: String,
    val slug: String,
    @ColumnInfo(name = "image_url") val imageUrl: String,
    val quantity: Double,
)

@Entity(
    tableName = "shopping_list_items",
    foreignKeys = [
        ForeignKey(
            entity = ShoppingListEntity::class,
            parentColumns = ["id"],
            childColumns = ["list_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ]
)
data class ShoppingListItemEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "list_id", index = true) val listId: String,
    val quantity: Double,
    val note: String,
    val display: String,
    val checked: Boolean,
    val position: Int,
    @ColumnInfo(name = "label_id") val labelId: String?,
    @Embedded(prefix = "unit_") val unit: ShoppingUnitColumns?,
    @Embedded(prefix = "food_") val food: ShoppingFoodColumns?,
    @Embedded(prefix = "lbl_") val label: ShoppingLabelColumns?,
    /** JSON array of how much of the item each linked recipe accounts for. */
    @ColumnInfo(name = "recipe_refs") val recipeRefs: String,
)

data class ShoppingUnitColumns(
    val id: String,
    val name: String,
    @ColumnInfo(name = "plural_name") val pluralName: String?,
    val abbreviation: String,
    @ColumnInfo(name = "use_abbreviation") val useAbbreviation: Boolean,
)

data class ShoppingFoodColumns(
    val id: String,
    val name: String,
    @ColumnInfo(name = "plural_name") val pluralName: String?,
    @ColumnInfo(name = "label_id") val labelId: String?,
    @ColumnInfo(name = "label_name") val labelName: String?,
)

data class ShoppingLabelColumns(
    val id: String,
    val name: String,
    val color: String,
)

data class ShoppingListWithRelations(
    @Embedded val list: ShoppingListEntity,
    @Relation(parentColumns = ["id"], entityColumns = ["list_id"])
    val items: List<ShoppingListItemEntity>,
    @Relation(parentColumns = ["id"], entityColumns = ["list_id"])
    val recipes: List<ShoppingListRecipeEntity>,
)
