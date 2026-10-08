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
    tint: Color = Color.White
): Modifier = drawWithCache {
    val line = 0.65.dp.toPx()
    val inset = line / 2f
    val outlineSize = Size((size.width - line).coerceAtLeast(0f), (size.height - line).coerceAtLeast(0f))
    val corner = CornerRadius(radius.toPx(), radius.toPx())
    val opacity = strength.coerceIn(0f, 1f)
    onDrawWithContent {
        drawContent()
        listOf(5f to 0.035f, 3f to 0.055f, 1.5f to 0.10f, 0.65f to 0.82f).forEach { (width, alpha) ->
            drawRoundRect(
                color = tint.copy(alpha = alpha * opacity),
                topLeft = Offset(inset, inset),
                size = outlineSize,
                cornerRadius = corner,
                style = Stroke(width.dp.toPx())
            )
        }
    }
}
