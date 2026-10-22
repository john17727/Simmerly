package dev.juanrincon.simmerly.shoppinglists.presentation.mappers

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import dev.juanrincon.simmerly.shoppinglists.aFood
import dev.juanrincon.simmerly.shoppinglists.aLabel
import dev.juanrincon.simmerly.shoppinglists.aRecipe
import dev.juanrincon.simmerly.shoppinglists.aRef
import dev.juanrincon.simmerly.shoppinglists.aShoppingList
import dev.juanrincon.simmerly.shoppinglists.aUnit
import dev.juanrincon.simmerly.shoppinglists.anItem
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.ItemGrouping
import dev.juanrincon.simmerly.shoppinglists.presentation.detail.models.SectionKind
import dev.juanrincon.simmerly.shoppinglists.presentation.list.models.ListCardFooterUi
import kotlin.test.Test

class PresentationMappersTest {

    // region Card footers

    @Test
    fun listWithoutItemsHasEmptyFooter() {
        assertThat(aShoppingList().toCardUi().footer).isEqualTo(ListCardFooterUi.Empty)
    }

    @Test
    fun listWithOnlyOneRecipesItemsHasOneRecipeFooter() {
        val list = aShoppingList(
            items = List(3) { anItem(id = "i$it", recipeRefs = listOf(aRef())) },
            recipes = listOf(aRecipe(quantity = 2.0)),
        )
        val footer = list.toCardUi().footer as ListCardFooterUi.OneRecipe
        assertThat(footer.itemCount).isEqualTo(3)
        assertThat(footer.quantity).isEqualTo(2)
    }

    @Test
    fun listWithSeveralRecipesOnlyHasSeveralRecipesFooterCappedAtThreeAvatars() {
        val recipes = List(4) { aRecipe(id = "r$it", name = "Recipe $it") }
        val list = aShoppingList(
            items = recipes.map { anItem(id = it.recipeId, recipeRefs = listOf(aRef(it.recipeId))) },
            recipes = recipes,
        )
        val footer = list.toCardUi().footer as ListCardFooterUi.SeveralRecipes
        assertThat(footer.recipes.size).isEqualTo(3)
        assertThat(footer.recipeCount).isEqualTo(4)
    }

    @Test
    fun listWithoutRecipesHasAddedByHandFooter() {
        val list = aShoppingList(items = listOf(anItem(note = "Paper towels"), anItem(id = "2", food = aFood(name = "olive oil"))))
        val footer = list.toCardUi().footer as ListCardFooterUi.AddedByHand
        assertThat(footer.itemNames).containsExactly("Paper towels", "olive oil")
    }

    @Test
    fun mixedListHasRecipesAndExtrasFooterWithCounts() {
        val list = aShoppingList(
            items = listOf(
                anItem(id = "1", recipeRefs = listOf(aRef())),
                anItem(id = "2", recipeRefs = listOf(aRef())),
                anItem(id = "3", note = "Napkins"),
            ),
            recipes = listOf(aRecipe()),
        )
        val footer = list.toCardUi().footer as ListCardFooterUi.RecipesAndExtras
        assertThat(footer.fromRecipesCount).isEqualTo(2)
        assertThat(footer.byHandCount).isEqualTo(1)
    }

    @Test
    fun cardProgressIsShareOfCheckedItems() {
        val list = aShoppingList(items = listOf(anItem(id = "1", checked = true), anItem(id = "2"), anItem(id = "3"), anItem(id = "4")))
        val card = list.toCardUi()
        assertThat(card.checkedCount).isEqualTo(1)
        assertThat(card.progress).isEqualTo(0.25f)
    }

    // endregion

    // region Sections

    @Test
    fun aisleSectionsFollowListLabelOrderThenNameWithNoAisleLast() {
        val produce = aLabel("produce", "Produce")
        val dairy = aLabel("dairy", "Dairy")
        val bakery = aLabel("bakery", "Bakery")
        val list = aShoppingList(
            items = listOf(
                anItem(id = "1", note = "loose", position = 0),
                anItem(id = "2", label = produce, position = 1),
                anItem(id = "3", label = bakery, position = 2),
                anItem(id = "4", label = dairy, position = 3),
                anItem(id = "5", label = produce, checked = true, position = 4),
            ),
            labelOrder = listOf("dairy", "produce"),
        )
        val sections = list.toSections(ItemGrouping.ByAisle)
        assertThat(sections.map { it.title to it.kind }).containsExactly(
            "Dairy" to SectionKind.Aisle,
            "Produce" to SectionKind.Aisle,
            "Bakery" to SectionKind.Aisle,
            "" to SectionKind.NoAisle,
        )
        // The checked produce item belongs to the checked card, not the aisle.
        assertThat(sections[1].items.map { it.id }).containsExactly("2")
    }

    @Test
    fun recipeSectionsPutEachItemUnderItsFirstRecipeAndHandAddedLast() {
        val list = aShoppingList(
            items = listOf(
                anItem(id = "1", recipeRefs = listOf(aRef("b"), aRef("a"))),
                anItem(id = "2", recipeRefs = listOf(aRef("a"))),
                anItem(id = "3", note = "Napkins"),
            ),
            recipes = listOf(aRecipe("a", "Shakshuka"), aRecipe("b", "Pancakes")),
        )
        val sections = list.toSections(ItemGrouping.ByRecipe)
        assertThat(sections.map { it.title to it.items.map { item -> item.id } }).containsExactly(
            "Shakshuka" to listOf("2"),
            "Pancakes" to listOf("1"),
            "" to listOf("3"),
        )
        assertThat(sections.last().kind).isEqualTo(SectionKind.AddedByHand)
    }

    // endregion

    // region Item text

    @Test
    fun foodItemSplitsQuantityUnitAndPluralFood() {
        val list = aShoppingList()
        val eggs = anItem(quantity = 10.0, food = aFood(name = "egg", pluralName = "eggs"), note = "large").toItemUi(list)
        assertThat(eggs.quantityText).isEqualTo("10")
        assertThat(eggs.name).isEqualTo("eggs")
        assertThat(eggs.note).isEqualTo("large")

        val ricotta = anItem(quantity = 2.0, unit = aUnit(), food = aFood(name = "ricotta")).toItemUi(list)
        assertThat(ricotta.quantityText).isEqualTo("2 cups")
        assertThat(ricotta.name).isEqualTo("ricotta")
    }

    @Test
    fun noteOnlyItemHidesALoneQuantityOfOne() {
        val item = anItem(note = "Paper napkins").toItemUi(aShoppingList())
        assertThat(item.quantityText).isEqualTo("")
        assertThat(item.name).isEqualTo("Paper napkins")
        assertThat(item.note).isNull()
    }

    @Test
    fun attributionNamesRecipeWithMultiplierOrPerRecipeAmounts() {
        val list = aShoppingList(recipes = listOf(aRecipe("a", "Shakshuka"), aRecipe("b", "Pancakes", quantity = 2.0)))
        assertThat(anItem(recipeRefs = listOf(aRef("a"))).toItemUi(list).attribution).isEqualTo("Shakshuka")
        assertThat(anItem(recipeRefs = listOf(aRef("b"))).toItemUi(list).attribution).isEqualTo("Pancakes ×2")
        assertThat(
            anItem(recipeRefs = listOf(aRef("a", 6.0), aRef("b", 4.0))).toItemUi(list).attribution
        ).isEqualTo("Shakshuka (6), Pancakes (4)")
    }

    @Test
    fun formatQuantityUsesWholeNumbersCommonFractionsOrDecimals() {
        assertThat(formatQuantity(2.0)).isEqualTo("2")
        assertThat(formatQuantity(1.5)).isEqualTo("1½")
        assertThat(formatQuantity(1.0 / 3)).isEqualTo("⅓")
        assertThat(formatQuantity(1.15)).isEqualTo("1.15")
        assertThat(formatEditableQuantity(0.5)).isEqualTo("0.5")
    }

    @Test
    fun cardFooterTypeForAllRecipeItemsWithSingleRecipeIsOneRecipe() {
        val list = aShoppingList(items = listOf(anItem(recipeRefs = listOf(aRef()))), recipes = listOf(aRecipe()))
        assertThat(list.toCardUi().footer).isInstanceOf(ListCardFooterUi.OneRecipe::class)
    }

    // endregion
}
