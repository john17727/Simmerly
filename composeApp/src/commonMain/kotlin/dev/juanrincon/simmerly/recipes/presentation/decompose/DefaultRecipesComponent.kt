package dev.juanrincon.simmerly.recipes.presentation.decompose

import com.arkivanov.decompose.ComponentContext

class DefaultRecipesComponent(componentContext: ComponentContext) : RecipesComponent,
    ComponentContext by componentContext {
}