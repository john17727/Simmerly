package dev.juanrincon.simmerly.recipes.presentation.decompose

import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.value.Value
import dev.juanrincon.simmerly.core.presentation.AppBarConfig
import dev.juanrincon.simmerly.recipes.presentation.comments.decompose.RecipeCommentsComponent
import dev.juanrincon.simmerly.recipes.presentation.details.decompose.RecipeDetailsComponent
import dev.juanrincon.simmerly.recipes.presentation.list.decompose.RecipeListComponent

@OptIn(ExperimentalDecomposeApi::class)
interface RecipesComponent {

    val panels: Value<ChildPanels<*, RecipeListComponent, *, RecipeDetailsComponent, *, RecipeCommentsComponent>>

    fun setMode(mode: ChildPanelsMode)

    val appBarConfig: Value<AppBarConfig>
}