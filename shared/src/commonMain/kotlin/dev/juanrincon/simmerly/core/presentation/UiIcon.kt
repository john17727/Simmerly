package dev.juanrincon.simmerly.core.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

sealed interface UiIcon {
    data class Vector(val image: ImageVector) : UiIcon
    data class Drawable(val res: DrawableResource) : UiIcon

    @Composable
    fun painter(): Painter = when (this) {
        is Vector -> rememberVectorPainter(image)
        is Drawable -> painterResource(res)
    }
}