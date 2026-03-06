package dev.juanrincon.simmerly.recipes.presentation.details

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.ImageData
import com.mohamedrejeb.richeditor.model.ImageLoader

object Coil3ImageLoader : ImageLoader {

    @OptIn(ExperimentalRichTextApi::class)
    @Composable
    override fun load(model: Any): ImageData? {
        val painter = rememberAsyncImagePainter(model = model)

        var imageData by remember {
            mutableStateOf<ImageData?>(null)
        }

        LaunchedEffect(painter.state) {
            painter.state.collect { state ->
                imageData =
                    if (state is AsyncImagePainter.State.Success)
                        ImageData(
                            painter = state.painter
                        )
                    else
                        null
            }
        }

        return imageData
    }

}
