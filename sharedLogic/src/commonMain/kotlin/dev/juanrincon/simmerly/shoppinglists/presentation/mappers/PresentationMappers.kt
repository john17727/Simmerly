package dev.juanrincon.simmerly.shoppinglists.presentation.mappers

import dev.juanrincon.simmerly.shoppinglists.domain.model.LinkedRecipe
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingFood
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingList
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingListItem
import dev.juanrincon.simmerly.shoppinglists.domain.model.ShoppingUnit
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.FoodOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemSectionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.LinkedRecipeUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.SectionKind
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ShoppingItemUi
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.UnitOptionUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ListCardFooterUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.RecipeThumbUi
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ShoppingListCardUi
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.math.roundToLong

/** How many recipe avatars a card stacks before switching to a "+N" counter. */
private const val CARD_RECIPE_AVATARS = 3

/** How many item names the "Added by hand" footer previews. */
private const val CARD_ITEM_PREVIEW = 3

fun ShoppingList.toCardUi(): ShoppingListCardUi {
    val checked = items.count { it.checked }
    return ShoppingListCardUi(
        id = id,
        name = name,
        ownerId = userId,
        itemCount = items.size,
        checkedCount = checked,
        progress = if (items.isEmpty()) 0f else checked.toFloat() / items.size,
        footer = cardFooter(),
    )
}

private fun ShoppingList.cardFooter(): ListCardFooterUi {
    if (items.isEmpty()) return ListCardFooterUi.Empty
    val linked = recipes.filter { it.quantity > 0 }
    val fromRecipes = items.count { it.recipeRefs.isNotEmpty() }
    val byHand = items.size - fromRecipes
    val thumbs = linked.take(CARD_RECIPE_AVATARS).map { it.toThumbUi() }
    return when {
        linked.isEmpty() || fromRecipes == 0 -> ListCardFooterUi.AddedByHand(
            itemNames = items.take(CARD_ITEM_PREVIEW).map { it.shortName() },
            itemCount = items.size,
        )

        byHand > 0 -> ListCardFooterUi.RecipesAndExtras(
            recipes = thumbs,
            recipeCount = linked.size,
            recipeNames = linked.map { it.name },
            fromRecipesCount = fromRecipes,
            byHandCount = byHand,
        )

        linked.size == 1 -> ListCardFooterUi.OneRecipe(
            recipe = linked.single().toThumbUi(),
            itemCount = items.size,
            quantity = linked.single().quantity.roundToInt(),
        )

        else -> ListCardFooterUi.SeveralRecipes(
            recipes = thumbs,
            recipeCount = linked.size,
            recipeNames = linked.map { it.name },
        )
    }
}

private fun LinkedRecipe.toThumbUi() = RecipeThumbUi(recipeId, name, imageUrl)

/** The item's food, or its note for a note-only item — what a preview line names it by. */
private fun ShoppingListItem.shortName(): String =
    food?.name ?: note.ifBlank { display }

fun ShoppingList.toLinkedRecipesUi(): List<LinkedRecipeUi> =
    recipes.filter { it.quantity > 0 }.map { recipe ->
        LinkedRecipeUi(
            recipeId = recipe.recipeId,
            name = recipe.name,
            imageUrl = recipe.imageUrl,
            quantity = recipe.quantity.roundToInt(),
            itemCount = items.count { item -> item.recipeRefs.any { it.recipeId == recipe.recipeId } },
        )
    }

/**
 * Groups the unchecked items into sections. By aisle, sections follow the list's own label order,
 * then any other labels alphabetically, with unlabelled items last under "No aisle". By recipe,
 * each item sits under the first recipe it came from, with hand-added items last.
 */
fun ShoppingList.toSections(grouping: ItemGrouping): List<ItemSectionUi> {
    val unchecked = items.filterNot { it.checked }.sortedBy { it.position }
    return when (grouping) {
        ItemGrouping.ByAisle -> aisleSections(unchecked)
        ItemGrouping.ByRecipe -> recipeSections(unchecked)
    }
}

private fun ShoppingList.aisleSections(unchecked: List<ShoppingListItem>): List<ItemSectionUi> {
    val labelNames = items.mapNotNull { it.label }.associate { it.id to it.name }
    val (labelled, unlabelled) = unchecked.partition { it.labelId != null && it.labelId in labelNames }
    val byLabel = labelled.groupBy { it.labelId!! }
    val orderedIds = labelOrder.filter { it in byLabel } +
        byLabel.keys.filterNot { it in labelOrder }.sortedBy { labelNames.getValue(it).lowercase() }
    return orderedIds.map { labelId ->
        ItemSectionUi(
            key = "label-$labelId",
            kind = SectionKind.Aisle,
            title = labelNames.getValue(labelId),
            items = byLabel.getValue(labelId).map { it.toItemUi(this) },
        )
    } + listOfNotNull(
        unlabelled.takeIf { it.isNotEmpty() }?.let { items ->
            ItemSectionUi("no-aisle", SectionKind.NoAisle, "", items.map { it.toItemUi(this) })
        }
    )
}

private fun ShoppingList.recipeSections(unchecked: List<ShoppingListItem>): List<ItemSectionUi> {
    val recipeIds = recipes.map { it.recipeId }.toSet()
    val (fromRecipes, byHand) = unchecked.partition { item ->
        item.recipeRefs.any { it.recipeId in recipeIds }
    }
    val byRecipe = fromRecipes.groupBy { item -> item.recipeRefs.first { it.recipeId in recipeIds }.recipeId }
    return recipes.filter { it.recipeId in byRecipe }.map { recipe ->
        ItemSectionUi(
            key = "recipe-${recipe.recipeId}",
            kind = SectionKind.Recipe,
            title = recipe.name,
            items = byRecipe.getValue(recipe.recipeId).map { it.toItemUi(this) },
        )
    } + listOfNotNull(
        byHand.takeIf { it.isNotEmpty() }?.let { items ->
            ItemSectionUi("by-hand", SectionKind.AddedByHand, "", items.map { it.toItemUi(this) })
        }
    )
}

fun ShoppingList.checkedItemsUi(): List<ShoppingItemUi> =
    items.filter { it.checked }.sortedBy { it.position }.map { it.toItemUi(this) }

fun ShoppingListItem.toItemUi(list: ShoppingList): ShoppingItemUi {
    val unitText = unit?.displayName(quantity)
    val food = food
    return if (food != null) {
        ShoppingItemUi(
            id = id,
            quantityText = listOfNotNull(quantity.takeIf { it > 0 }?.let(::formatQuantity), unitText)
                .joinToString(" "),
            name = if (quantity > 1 && unit == null) food.pluralName ?: food.name else food.name,
            nameIsFood = true,
            note = note.takeIf { it.isNotBlank() },
            attribution = attribution(list),
            checked = checked,
        )
    } else {
        // A note-only item reads as plain text; a lone "1" in front of it adds nothing.
        val showQuantity = unit != null || (quantity > 0 && quantity != 1.0)
        ShoppingItemUi(
            id = id,
            quantityText = if (showQuantity) {
                listOfNotNull(formatQuantity(quantity), unitText).joinToString(" ")
            } else "",
            name = note.ifBlank { display },
            nameIsFood = false,
            note = null,
            attribution = attribution(list),
            checked = checked,
        )
    }
}

/**
 * "Shakshuka" for one recipe, "Lemon ricotta pancakes ×2" when that recipe was added twice, and
 * "Shakshuka (6), Lemon ricotta pancakes (4)" when several recipes share the item.
 */
private fun ShoppingListItem.attribution(list: ShoppingList): String? {
    val sources = recipeRefs.mapNotNull { ref ->
        list.recipes.firstOrNull { it.recipeId == ref.recipeId }?.let { recipe -> ref to recipe }
    }
    return when (sources.size) {
        0 -> null
        1 -> {
            val recipe = sources.single().second
            if (recipe.quantity > 1) "${recipe.name} ×${formatQuantity(recipe.quantity)}" else recipe.name
        }

        else -> sources.joinToString { (ref, recipe) -> "${recipe.name} (${formatQuantity(ref.quantity)})" }
    }
}

fun ShoppingUnit.displayName(quantity: Double): String = when {
    useAbbreviation && abbreviation.isNotBlank() -> abbreviation
    quantity > 1 && !pluralName.isNullOrBlank() -> pluralName
    else -> name
}

fun ShoppingUnit.toOptionUi() = UnitOptionUi(id = id, name = name)

fun ShoppingFood.toOptionUi() = FoodOptionUi(id = id, name = name, aisle = labelName)

/** Whether [term] names this food, singular or plural, ignoring case. */
fun ShoppingFood.isNamed(term: String): Boolean =
    name.equals(term, ignoreCase = true) || pluralName?.equals(term, ignoreCase = true) == true

/**
 * What the add-item bar searches foods for: [text] without a leading quantity ("2", "1½",
 * "1/2", "1 1/2") or, after it, a word naming one of [units]. [hasExtras] is true when anything
 * was stripped, so the text says more than just the food's name.
 */
data class AddItemQuery(val term: String, val hasExtras: Boolean)

private val LEADING_QUANTITY = Regex("""^(\d+\s+\d+/\d+|\d+/\d+|\d*[½¼¾⅓⅔⅛]|\d+(?:[.,]\d+)?)\s*""")

fun addItemQuery(text: String, units: List<ShoppingUnit>): AddItemQuery {
    val trimmed = text.trim()
    var rest = trimmed
    LEADING_QUANTITY.find(rest)?.let { rest = rest.substring(it.range.last + 1) }
    val firstWord = rest.substringBefore(' ')
    val unitWord = firstWord.trimEnd('.')
    val namesUnit = rest.contains(' ') && units.any { unit ->
        listOfNotNull(unit.name, unit.pluralName, unit.abbreviation.takeIf { it.isNotBlank() })
            .any { it.equals(unitWord, ignoreCase = true) }
    }
    if (namesUnit) rest = rest.substringAfter(' ')
    val term = rest.trim()
    return AddItemQuery(term = term, hasExtras = term != trimmed)
}

private val FRACTIONS = listOf(
    0.25 to "¼", 0.5 to "½", 0.75 to "¾", 1.0 / 3 to "⅓", 2.0 / 3 to "⅔", 0.125 to "⅛",
)

/** "2", "1½", "⅓" or, failing a common fraction, up to two decimals ("1.15"). */
fun formatQuantity(quantity: Double): String {
    val whole = quantity.toLong()
    val fraction = quantity - whole
    if (abs(fraction) < 0.01) return whole.toString()
    FRACTIONS.firstOrNull { abs(it.first - fraction) < 0.01 }?.let { (_, glyph) ->
        return if (whole == 0L) glyph else "$whole$glyph"
    }
    return formatEditableQuantity(quantity)
}

/** The editor's quantity field: plain digits, so it can be edited and parsed back. */
fun formatEditableQuantity(quantity: Double): String {
    val hundredths = (quantity * 100).roundToLong()
    if (hundredths % 100 == 0L) return (hundredths / 100).toString()
    val decimals = (hundredths % 100).toString().padStart(2, '0').trimEnd('0')
    return "${hundredths / 100}.$decimals"
}
