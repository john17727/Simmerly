package dev.juanrincon.simmerly.mealplan.presentation.orbit

import dev.juanrincon.simmerly.mealplan.domain.MealPlanEntry
import dev.juanrincon.simmerly.recipes.domain.model.Ingredient
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListRowUi
import dev.juanrincon.simmerly.recipes.presentation.details.orbit.AddToListTarget
import dev.juanrincon.simmerly.shoppinglists.presentation.list.orbit.ListFilter
import kotlinx.datetime.LocalDate

enum class CartScope { Day, Week }

/** [entryId] set = just that one entry (from a card's menu); the scope switch is then hidden. */
data class MealPlanCartArgs(
    val date: LocalDate,
    val scope: CartScope = CartScope.Day,
    val entryId: Int? = null,
)

/** A planned recipe the cart may add; [included] is the checkbox. */
data class CartEntry(
    val entry: MealPlanEntry,
    val included: Boolean,
    /** An "any day" entry that isn't on a day yet: shown in the week, off by default. */
    val unscheduled: Boolean,
)

data class CartIngredient(val entryId: Int, val ingredient: Ingredient)

data class CartRow(
    val key: String,
    /** Amount summed over the sources, in the shared unit; null when the ingredient has none. */
    val quantity: Double?,
    val unitName: String?,
    val unitPluralName: String?,
    val food: String,
    /** "For Pizza, Tacos" when combined from several recipes, else the ingredient note. */
    val note: String,
    val selected: Boolean,
    val sources: List<CartIngredient>,
)

data class CartGroup(
    /** Null when combined into one list. */
    val entry: MealPlanEntry?,
    val rows: List<CartRow>,
)

data class MealPlanCartState(
    val scope: CartScope = CartScope.Day,
    val date: LocalDate = LocalDate(1970, 1, 1),
    val weekStart: LocalDate = date,
    val weekEnd: LocalDate = date,
    val isLoading: Boolean = true,
    val candidates: List<CartEntry> = emptyList(),
    /** Notes in scope: listed, but nothing to buy. */
    val notes: List<MealPlanEntry> = emptyList(),
    /** Ingredients per recipe id; a recipe missing here hasn't loaded (or failed to). */
    val ingredients: Map<String, List<Ingredient>> = emptyMap(),
    val combine: Boolean = true,
    /** Row keys whose default selection the user flipped. */
    val toggledKeys: Set<String> = emptySet(),
    val filter: ListFilter = ListFilter.Mine,
    val lists: List<AddToListRowUi>? = null,
    val target: AddToListTarget? = null,
    val creatingNew: Boolean = false,
    val newListName: String = "",
    val isSubmitting: Boolean = false,
) {
    val includedEntries: List<CartEntry> get() = candidates.filter { it.included && it.entry.recipe != null }

    val groups: List<CartGroup>
        get() {
            val groups = if (combine) combinedGroups() else includedEntries.map { separateGroup(it) }
            return groups.filter { it.rows.isNotEmpty() }
        }

    val rows: List<CartRow> get() = groups.flatMap { it.rows }
    val selectedCount: Int get() = rows.count { it.selected }
    val allSelected: Boolean get() = rows.isNotEmpty() && rows.all { it.selected }

    val canAdd: Boolean
        get() = !isSubmitting && selectedCount > 0 &&
            (target != null || (creatingNew && newListName.isNotBlank()))

    private fun isOn(key: String) = key !in toggledKeys

    private fun separateGroup(c: CartEntry): CartGroup {
        val id = c.entry.id
        val rows = ingredients[c.entry.recipe!!.id].orEmpty().map { ing ->
            val key = "g:$id:" + ing.referenceId
            CartRow(
                key = key,
                quantity = ing.quantity?.times(c.entry.recipeScale),
                unitName = ing.unit?.name,
                unitPluralName = ing.unit?.pluralName,
                food = ing.food?.name ?: ing.display,
                note = ing.note.orEmpty(),
                selected = isOn(key),
                sources = listOf(CartIngredient(id, ing)),
            )
        }
        return CartGroup(c.entry, rows)
    }

    private fun combinedGroups(): List<CartGroup> {
        data class Acc(var qty: Double?, val first: Ingredient, val from: MutableList<String>, val notes: MutableList<String>, val sources: MutableList<CartIngredient>)
        val acc = LinkedHashMap<String, Acc>()
        for (c in includedEntries) {
            val recipe = c.entry.recipe!!
            for (ing in ingredients[recipe.id].orEmpty()) {
                val food = ing.food?.id ?: ing.food?.name?.lowercase() ?: ing.display.lowercase()
                val key = "c:$food|" + ing.unit?.id.orEmpty()
                val scaled = ing.quantity?.times(c.entry.recipeScale)
                val a = acc.getOrPut(key) { Acc(null, ing, mutableListOf(), mutableListOf(), mutableListOf()) }
                if (scaled != null) a.qty = (a.qty ?: 0.0) + scaled
                if (recipe.name !in a.from) a.from += recipe.name
                ing.note?.takeIf { it.isNotBlank() && it !in a.notes }?.let { a.notes += it }
                a.sources += CartIngredient(c.entry.id, ing)
            }
        }
        val rows = acc.map { (key, a) ->
            CartRow(
                key = key,
                quantity = a.qty,
                unitName = a.first.unit?.name,
                unitPluralName = a.first.unit?.pluralName,
                food = a.first.food?.name ?: a.first.display,
                note = if (a.from.size > 1) "For " + a.from.joinToString(", ") else a.notes.joinToString("; "),
                selected = isOn(key),
                sources = a.sources,
            )
        }
        return listOf(CartGroup(null, rows))
    }
}
