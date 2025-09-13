package dev.juanrincon.simmerly.core.presentation

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.node.DrawModifierNode
import androidx.compose.ui.node.LayoutAwareModifierNode
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.invalidateDraw
import androidx.compose.ui.platform.InspectorInfo
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

fun Modifier.shimmer(colors: List<Color>, shape: Shape = RectangleShape): Modifier = this.then(
    ShimmerModifierElement(colors, shape)
)

private data class ShimmerModifierElement(
    val colors: List<Color>,
    val shape: Shape
) : ModifierNodeElement<ShimmerModifierNode>() {
    override fun create(): ShimmerModifierNode = ShimmerModifierNode(colors, shape)

    override fun update(node: ShimmerModifierNode) {
        var changed = false
        if (node.colors !== colors) {
            node.colors = colors
            changed = true
        }
        if (node.shape != shape) {
            node.shape = shape
            changed = true
        }
        if (changed) node.invalidateDraw()
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "shimmer"
        properties["colors"] = colors
        properties["shape"] = shape
    }
}

private class ShimmerModifierNode(
    var colors: List<Color>,
    var shape: Shape
) : Modifier.Node(), DrawModifierNode, LayoutAwareModifierNode {

    private val anim: Animatable<Float, AnimationVector1D> = Animatable(0f)
    private var animationJob: Job? = null
    private var lastWidth: Float = 0f

    override fun onDetach() {
        super.onDetach()
        animationJob?.cancel()
        animationJob = null
    }

    override fun onPlaced(coordinates: androidx.compose.ui.layout.LayoutCoordinates) {
        val width = coordinates.size.width.toFloat()
        if (width > 0f && width != lastWidth) {
            lastWidth = width
            restartAnimation(width)
        }
    }

    private fun restartAnimation(width: Float) {
        animationJob?.cancel()
        animationJob = coroutineScope.launch {
            anim.snapTo(-2f * width)
            anim.animateTo(
                targetValue = 2f * width,
                animationSpec = infiniteRepeatable(animation = tween(durationMillis = 1000))
            )
        }
    }

    override fun ContentDrawScope.draw() {
        val width = size.width
        val height = size.height
        if (width == 0f || height == 0f) return
        val startX = anim.value
        val brush = Brush.linearGradient(
            colors = colors,
            start = Offset(startX, 0f),
            end = Offset(startX + width, height)
        )
        val outline = shape.createOutline(
            size = Size(width, height),
            layoutDirection = layoutDirection,
            density = this
        )
        when (outline) {
            is Outline.Rectangle -> {
                drawRect(
                    brush = brush,
                    size = Size(width, height)
                )
            }
            is Outline.Rounded -> {
                val path = androidx.compose.ui.graphics.Path().apply {
                    addRoundRect(outline.roundRect)
                }
                drawPath(
                    path = path,
                    brush = brush
                )
            }
            is Outline.Generic -> {
                drawPath(
                    path = outline.path,
                    brush = brush
                )
            }
        }
    }
}