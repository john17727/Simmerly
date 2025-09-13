package dev.juanrincon.simmerly.navigation.app

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import dev.juanrincon.simmerly.core.presentation.AppBarConfig
import dev.juanrincon.simmerly.core.presentation.ifTrue
import dev.juanrincon.simmerly.recipes.presentation.decompose.RecipesContent
import dev.juanrincon.simmerly.recipes.presentation.list.mvikotlin.RecipeListStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(component: AppComponent, modifier: Modifier = Modifier) {
    Children(
        stack = component.stack,
        modifier = modifier,
        animation = stackAnimation(fade()),
    ) {
        val child = it.instance
        val appBarConfig = when(child) {
            is AppComponent.Child.RecipesChild -> child.component.appBarConfig.subscribeAsState().value
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(appBarConfig.title) },
                    actions = {
                        appBarConfig.actions.forEach { action ->
                            IconButton(onClick = action.onClick) {
                                Icon(action.icon, contentDescription = action.contentDescription)
                            }
                        }
                    },
//                    scrollBehavior = scrollBehavior
                )
            },
            modifier = modifier//.nestedScroll(scrollBehavior.nestedScrollConnection)
//                .ifTrue(
//                windowSizeClass.isWidthAtLeastBreakpoint(
//                    WindowSizeClass.WIDTH_DP_EXPANDED_LOWER_BOUND
//                )
//            ) {
//                padding(end = 8.dp).then(clip(shape = MaterialTheme.shapes.medium))
//            },
        ) { paddingValues ->
            when (val child = it.instance) {
                is AppComponent.Child.RecipesChild -> RecipesContent(child.component, modifier = Modifier.padding(paddingValues))
            }
        }
    }
}