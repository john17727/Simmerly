package dev.juanrincon.simmerly.recipes.presentation.decompose

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanels
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
        }
    )
}