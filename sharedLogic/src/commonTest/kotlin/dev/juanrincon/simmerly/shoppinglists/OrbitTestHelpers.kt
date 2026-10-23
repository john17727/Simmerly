package dev.juanrincon.simmerly.shoppinglists

import org.orbitmvi.orbit.OrbitContainerHost
import org.orbitmvi.orbit.test.Item
import org.orbitmvi.orbit.test.OrbitScopedTestContextInternal

/** Skips states until one matches [predicate] — these ViewModels reduce several times per
 * action, and the intermediate states aren't what the tests are about. */
suspend fun <S : Any, E : Any, H : OrbitContainerHost<S, S, E>> OrbitScopedTestContextInternal<S, S, E, H>.awaitStateMatching(
    predicate: (S) -> Boolean
): S {
    while (true) {
        val item = awaitItem()
        if (item is Item.StateItem && predicate(item.value)) return item.value
    }
}

suspend fun <S : Any, E : Any, H : OrbitContainerHost<S, S, E>> OrbitScopedTestContextInternal<S, S, E, H>.awaitSideEffectSkippingStates(): E {
    while (true) {
        val item = awaitItem()
        if (item is Item.SideEffectItem) return item.value
    }
}
