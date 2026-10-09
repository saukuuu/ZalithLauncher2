package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Thin white outline with a soft halo; no blur, bitmap or extra dependency. */
fun Modifier.launcherGlow(
    radius: Dp = 14.dp,
    strength: Float = 0.75f,
    tint: Color = Color.White,
    lineWidth: Dp = 0.65.dp,
    haloWidth: Dp = 5.dp
): Modifier = drawWithCache {
    val line = lineWidth.toPx()
    val inset = line / 2f
    val outlineSize = Size((size.width - line).coerceAtLeast(0f), (size.height - line).coerceAtLeast(0f))
    val corner = CornerRadius(radius.toPx(), radius.toPx())
    val opacity = strength.coerceIn(0f, 1f)
    val layers = listOf(
        haloWidth.toPx() to 0.035f,
        (haloWidth * 0.6f).toPx() to 0.055f,
        (haloWidth * 0.3f).toPx() to 0.10f,
        line to 0.82f
    )
    onDrawWithContent {
        drawContent()
        layers.forEach { (width, alpha) ->
            drawRoundRect(
                color = tint.copy(alpha = alpha * opacity),
                topLeft = Offset(inset, inset),
                size = outlineSize,
                cornerRadius = corner,
                style = Stroke(width)
            )
        }
    }
}
