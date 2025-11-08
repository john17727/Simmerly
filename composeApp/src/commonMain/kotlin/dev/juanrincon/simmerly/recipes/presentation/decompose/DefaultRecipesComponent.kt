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
import com.arkivanov.decompose.value.MutableValue
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.lifecycle.coroutines.coroutineScope
import com.arkivanov.mvikotlin.core.store.StoreFactory
import dev.juanrincon.simmerly.core.presentation.AppBarAction
import dev.juanrincon.simmerly.core.presentation.AppBarConfig
import dev.juanrincon.simmerly.core.presentation.activateAndShowExtra
import dev.juanrincon.simmerly.core.presentation.asFlow
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
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import org.jetbrains.compose.resources.painterResource
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import simmerly.composeapp.generated.resources.Res
import simmerly.composeapp.generated.resources.right_panel_close
import simmerly.composeapp.generated.resources.right_panel_open

@OptIn(ExperimentalDecomposeApi::class, ExperimentalCoroutinesApi::class)
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

    override fun setMode(mode: ChildPanelsMode) {
        if (mode == ChildPanelsMode.SINGLE) {
            nav.dismissAndHideExtra()
        }
        nav.setMode(mode)
    }

    private val _appBarConfig = MutableValue(AppBarConfig(title = "Recipes", actions = emptyList()))
    override val appBarConfig: Value<AppBarConfig> = _appBarConfig

    init {
        coroutineScope().launch {
            panels.asFlow().flatMapLatest { p ->
                val list = p.main.instance
                val details = p.details?.instance
                val extra = p.extra?.instance

                // Build a base config for each mode
                val baseSingle = AppBarConfig(
                    title = if (details != null) "Details" else "Recipes",
                    showNavigationIcon = details != null,
                    onNavigationClick = if (details != null) ({ nav.pop() }) else null,
                    actions = if (details == null) listOf(
                        refreshAction(
                            list,
                            details
                        )
                    ) else emptyList()
                )

                val baseSplit = AppBarConfig(
                    title = "Recipes",
                    showNavigationIcon = false,
                    actions = listOf(refreshAction(list, details))
                )

                when (p.mode) {
                    ChildPanelsMode.SINGLE -> flowOf(baseSingle)
                    ChildPanelsMode.DUAL, ChildPanelsMode.TRIPLE -> {
                        // Combine the child’s dynamic actions and commentsEnabled
                        details?.commentsEnabled?.map { enabled ->

                            val actions = buildList {
                                add(refreshAction(list, details))
                                addAll(details.appBarActions)
                                if (enabled) {
                                    (p.details?.configuration as? DetailsConfig)?.recipeId?.let {
                                        add(toggleExtraAction(extra, it))
                                    }
                                }
                            }
                            baseSplit.copy(actions = actions)
                        }
                            ?: flowOf(baseSplit)
                    }
                }
            }.collect { _appBarConfig.value = it }
        }

        coroutineScope().launch {
            panels.asFlow()
                .flatMapLatest { p ->
                    val details = p.details?.instance
                    val hasExtra = p.extra != null

                    // If there is no details component, emit a single value meaning "nothing to dismiss"
                    val enabledFlow = details?.commentsEnabled ?: flowOf(true)

                    enabledFlow
                        .distinctUntilChanged() // avoid redundant repeats from the same details
                        .map { enabled -> enabled to hasExtra }
                }
                .distinctUntilChanged() // avoid re-triggering when (enabled, hasExtra) pair didn’t change
                .collect { (enabled, hasExtra) ->
                    if (!enabled && hasExtra) {
                        nav.dismissAndHideExtra()
                    }
                }
        }

        coroutineScope().launch {
            panels.asFlow()
                .flatMapLatest { p ->
                    val details = p.details
                    val enabledFlow = details?.instance?.commentsEnabled ?: flowOf(false)
                    val recipeId = (details?.configuration as? DetailsConfig)?.recipeId

                    enabledFlow
                        .distinctUntilChanged() // only react when enabled flips
                        .map { enabled -> enabled to recipeId }
                }
                .distinctUntilChanged() // avoid reprocessing same pair
                .collect { (enabled, recipeId) ->
                    if (enabled) {
                        recipeId?.let { id ->
                            // Prepare or refresh the extra for the current recipe
                            nav.updateExtra(ExtrasConfig(id))
                        }
                    }
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