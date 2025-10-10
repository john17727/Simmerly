package dev.juanrincon.simmerly.recipes.presentation.extras.decompose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import dev.juanrincon.simmerly.recipes.presentation.extras.mvikotlin.RecipeExtrasStore

@Composable
fun RecipeExtrasContent(component: RecipeExtrasComponent) {
    val state by component.state.collectAsState()

    val text = when (state) {
        is RecipeExtrasStore.State.Comments -> "Comments Coming Soon"
        is RecipeExtrasStore.State.Settings -> "Settings Coming Soon"
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text)
    }
}