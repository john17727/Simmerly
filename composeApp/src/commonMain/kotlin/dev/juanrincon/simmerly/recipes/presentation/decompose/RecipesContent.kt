package dev.juanrincon.simmerly.recipes.presentation.decompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.experimental.panels.ChildPanels
import com.arkivanov.decompose.extensions.compose.experimental.panels.HorizontalChildPanelsLayout
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import dev.juanrincon.simmerly.recipes.presentation.comments.decompose.RecipeCommentsContent
import dev.juanrincon.simmerly.recipes.presentation.details.decompose.RecipeDetailsContent
import dev.juanrincon.simmerly.recipes.presentation.list.decompose.RecipeListContent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RecipesContent(component: RecipesComponent, modifier: Modifier = Modifier) {
    val panels by component.panels.subscribeAsState()
    var panelMode = panels.mode


    ChildPanelsModeChangedEffect { mode ->
        component.setMode(mode)
    }
    ChildPanels(
        panels = component.panels,
        mainChild = {
            RecipeListContent(it.instance)
        },
        detailsChild = {
            RecipeDetailsContent(it.instance)
        },
        secondPanelPlaceholder = {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Select a recipe to see details")
            }
        },
        extraChild = {
            RecipeCommentsContent(it.instance)
        },
        layout = remember {
            HorizontalChildPanelsLayout(
                dualWeights = Pair(0.30F, 0.70F),
                tripleWeights = Triple(0.25F, 0.50F, 0.25F)
            )
        },
        modifier = when (panelMode) {
            ChildPanelsMode.DUAL -> modifier.padding(start = 16.dp, end = 16.dp)
            ChildPanelsMode.TRIPLE -> modifier.padding(start = 16.dp)
            else -> modifier
        }
    )
}