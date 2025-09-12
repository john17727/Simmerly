package dev.juanrincon.simmerly.recipes.presentation.decompose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanels
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanelsAnimators
import com.arkivanov.decompose.extensions.compose.experimental.panels.HorizontalChildPanelsLayout
import com.arkivanov.decompose.extensions.compose.experimental.stack.animation.fade
import dev.juanrincon.simmerly.recipes.presentation.details.decompose.RecipeDetailsContent
import dev.juanrincon.simmerly.recipes.presentation.list.decompose.RecipeListContent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RecipesContent(component: RecipesComponent) {
    ChildPanelsModeChangedEffect(component::setMode)
    ChildPanels(
        panels = component.panels,
        mainChild = {
            RecipeListContent(it.instance)
        },
        detailsChild = {
            RecipeDetailsContent(it.instance)
        },
        layout = remember { HorizontalChildPanelsLayout(dualWeights = Pair(1F, 3F)) },
        animators = remember { ChildPanelsAnimators(fade()) }
    )
}