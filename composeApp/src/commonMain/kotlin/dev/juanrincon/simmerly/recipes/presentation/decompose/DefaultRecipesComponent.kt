package dev.juanrincon.simmerly.recipes.presentation.decompose

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.router.panels.ChildPanels
import com.arkivanov.decompose.router.panels.ChildPanelsMode
import com.arkivanov.decompose.router.panels.Panels
import com.arkivanov.decompose.router.panels.PanelsNavigation
import com.arkivanov.decompose.router.panels.activateDetails
import com.arkivanov.decompose.router.panels.childPanels
import com.arkivanov.decompose.router.panels.pop
import com.arkivanov.decompose.router.panels.setMode
import com.arkivanov.decompose.value.Value
import com.arkivanov.decompose.value.operator.map
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.juanrincon.simmerly.core.presentation.AppBarAction
import dev.juanrincon.simmerly.core.presentation.AppBarConfig
import dev.juanrincon.simmerly.core.presentation.activateAndShowExtra
import dev.juanrincon.simmerly.core.presentation.dismissAndHideExtra
import dev.juanrincon.simmerly.core.presentation.updateExtra
import dev.juanrincon.simmerly.recipes.domain.RecipeRepository
import dev.juanrincon.simmerly.recipes.presentation.comments.decompose.DefaultRecipeCommentsComponent
import dev.juanrincon.simmerly.recipes.presentation.comments.decompose.RecipeCommentsComponent
import dev.juanrincon.simmerly.recipes.presentation.details.decompose.DefaultRecipeDetailsComponent
import dev.juanrincon.simmerly.recipes.presentation.details.decompose.RecipeDetailsComponent
import dev.juanrincon.simmerly.recipes.presentation.list.decompose.DefaultRecipeListComponent
import dev.juanrincon.simmerly.recipes.presentation.list.decompose.RecipeListComponent
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.jetbrains.compose.resources.painterResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.right_panel_close
import simmerly.composeapp.generated.resources.right_panel_open

@OptIn(ExperimentalDecomposeApi::class)
class DefaultRecipesComponent(componentContext: ComponentContext, storeFactory: StoreFactory) :
    RecipesComponent, ComponentContext by componentContext, KoinComponent {

    private val nav = PanelsNavigation<Unit, DetailsConfig, ExtrasConfig>()

    @OptIn(ExperimentalSerializationApi::class)
    override val panels: Value<ChildPanels<*, RecipeListComponent, *, RecipeDetailsComponent, *, RecipeCommentsComponent>> =
        childPanels(
            source = nav,
            serializers = Triple(
                Unit.serializer(),
                DetailsConfig.serializer(),
                ExtrasConfig.serializer()
            ),
            initialPanels = { Panels(main = Unit) },
            handleBackButton = true,
            mainFactory = { _, ctx ->
                DefaultRecipeListComponent(
                    componentContext = ctx,
                    storeFactory = storeFactory,
                    repository = get<RecipeRepository>(),
                    onRecipeSelected = { recipeId ->
                        nav.activateDetails(DetailsConfig(recipeId))
                        nav.updateExtra(ExtrasConfig(recipeId))
                    }
                )
            },
            detailsFactory = { cfg, ctx ->
                DefaultRecipeDetailsComponent(
                    recipeId = cfg.recipeId,
                    componentContext = ctx,
                    storeFactory = storeFactory,
                    repository = get<RecipeRepository>()
                )
            },
            extraFactory = { cfg, ctx ->
                DefaultRecipeCommentsComponent(
                    recipeId = cfg.recipeId,
                    componentContext = ctx,
                    storeFactory = storeFactory,
                    repository = get<RecipeRepository>()
                )
            }
        )

    override fun setMode(mode: ChildPanelsMode) = nav.setMode(mode)
    override val appBarConfig: Value<AppBarConfig> =
        panels.map { p ->
            val list = p.main.instance
            val details = p.details?.instance
            val extra = p.extra?.instance

            when (p.mode) {
                ChildPanelsMode.SINGLE -> {
                    if (details != null) {
                        // Pull the recipe title from the details component’s state (expose it as Value/StateFlow there)
                        val title = "Details" //(details.title ?: "Details")
                        AppBarConfig(
                            title = title,
                            showNavigationIcon = true,
                            onNavigationClick = { nav.pop() },
                            actions = emptyList(),
                        )
                    } else {
                        AppBarConfig(
                            title = "Recipes",
                            actions = listOf(refreshAction(list, details))
                        )
                    }
                }

                ChildPanelsMode.DUAL,
                ChildPanelsMode.TRIPLE -> {

                    AppBarConfig(
                        title = "Recipes",
                        showNavigationIcon = false,
                        actions = buildList {
                            // Always keep the list action in split mode
                            add(refreshAction(list, details))
                            // Optionally add details actions if your details component exposes them
                            // details?.appBarActions?.let { addAll(it) }
                            if (details != null) {
                                val recipeIdFromConfig =
                                    (p.details?.configuration as? DetailsConfig)?.recipeId
                                recipeIdFromConfig?.let {
                                    add(toggleExtraAction(extra, it))
                                }
                            }
                        }
                    )
                }
            }
        }

    @Serializable
    private data class DetailsConfig(val recipeId: String)

    @Serializable
    private data class ExtrasConfig(val recipeId: String)

    // Helper to build list refresh action
    private fun refreshAction(
        listComponent: RecipeListComponent,
        detailsComponent: RecipeDetailsComponent?
    ) = AppBarAction(
        icon = { Icon(Icons.Default.Refresh, contentDescription = "Refresh") },
        onClick = { listComponent.onEvent(RecipeListStore.Intent.OnRefresh) }
    )

    private fun toggleExtraAction(extraComponent: RecipeCommentsComponent?, recipeId: String) =
        AppBarAction(
            icon = {
                if (extraComponent != null) {
                    Icon(
                        painterResource(Res.drawable.right_panel_close),
                        contentDescription = "Close Right Panel"
                    )
                } else {
                    Icon(
                        painterResource(Res.drawable.right_panel_open),
                        contentDescription = "Open Right Panel"
                    )
                }
            },
            onClick = {
                if (extraComponent == null) {
                    nav.activateAndShowExtra(ExtrasConfig(recipeId))
                } else {
                    nav.dismissAndHideExtra()
                }
            }
        )
}