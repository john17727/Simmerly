package dev.juanrincon.simmerly.shoppinglists

import dev.juanrincon.simmerly.shoppinglists.domain.model.ItemRecipeRef
import dev.juanrincon.simmerly.shoppinglists.domain.model.LinkedRecipe
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingLabel
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingListItem
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit

fun aShoppingList(
    id: String = "list-1",
    name: String = "Sunday brunch",
    userId: String = "user-1",
    items: List<ShoppingListItem> = emptyList(),
    recipes: List<LinkedRecipe> = emptyList(),
    labelOrder: List<String> = emptyList(),
) = ShoppingList(id, name, userId, items, recipes, labelOrder)

fun anItem(
    id: String = "item-1",
    listId: String = "list-1",
    quantity: Double = 1.0,
    unit: ShoppingUnit? = null,
    food: ShoppingFood? = null,
    note: String = "",
    checked: Boolean = false,
    position: Int = 0,
    label: ShoppingLabel? = null,
    recipeRefs: List<ItemRecipeRef> = emptyList(),
) = ShoppingListItem(
    id = id,
    listId = listId,
    quantity = quantity,
    unit = unit,
    food = food,
    note = note,
    display = "",
    checked = checked,
    position = position,
    labelId = label?.id,
    label = label,
    recipeRefs = recipeRefs,
)

fun aRecipe(id: String = "recipe-1", name: String = "Shakshuka", quantity: Double = 1.0) =
    LinkedRecipe(recipeId = id, name = name, slug = name.lowercase(), imageUrl = "", quantity = quantity)

fun aRef(recipeId: String = "recipe-1", quantity: Double = 1.0) =
    ItemRecipeRef(id = "ref-$recipeId", recipeId = recipeId, quantity = quantity, scale = 1.0, note = null)

fun aFood(id: String = "food-1", name: String = "onion", pluralName: String? = null, labelId: String? = null) =
    ShoppingFood(id = id, name = name, pluralName = pluralName, labelId = labelId)

fun aUnit(id: String = "unit-1", name: String = "cup", pluralName: String? = "cups") =
    ShoppingUnit(id = id, name = name, pluralName = pluralName, abbreviation = "", useAbbreviation = false)

fun aLabel(id: String = "label-1", name: String = "Produce") =
    ShoppingLabel(id = id, name = name, color = "#959595")
