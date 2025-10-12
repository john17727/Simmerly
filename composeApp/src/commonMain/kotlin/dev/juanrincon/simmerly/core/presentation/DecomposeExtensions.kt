package dev.juanrincon.simmerly.core.presentation

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigator

@OptIn(ExperimentalDecomposeApi::class)
fun <MC : Any, DC : Any, EC : Any> PanelsNavigator<MC, DC, EC>.dismissAndHideExtra(
    onComplete: (newState: Panels<MC, DC, EC>, oldState: Panels<MC, DC, EC>) -> Unit = { _, _ -> },
) {
    navigate(
        transformer = { it.copy(extra = null, mode = ChildPanelsMode.DUAL) },
        onComplete = onComplete
    )
}

@OptIn(ExperimentalDecomposeApi::class)
fun <MC : Any, DC : Any, EC : Any> PanelsNavigator<MC, DC, EC>.activateAndShowExtra(
    extra: EC,
    onComplete: (newState: Panels<MC, DC, EC>, oldState: Panels<MC, DC, EC>) -> Unit = { _, _ -> },
) {
    navigate(
        transformer = { it.copy(extra = extra, mode = ChildPanelsMode.TRIPLE) },
        onComplete = onComplete
    )
}

@OptIn(ExperimentalDecomposeApi::class)
fun <MC : Any, DC : Any, EC : Any> PanelsNavigator<MC, DC, EC>.updateExtra(
    extra: EC,
    onComplete: (newState: Panels<MC, DC, EC>, oldState: Panels<MC, DC, EC>) -> Unit = { _, _ -> },
) {
    navigate(
        transformer = {
            if (it.extra != null && it.extra != extra) {
                it.copy(extra = extra)
            } else {
                it
            }
        },
        onComplete = onComplete
    )
}
