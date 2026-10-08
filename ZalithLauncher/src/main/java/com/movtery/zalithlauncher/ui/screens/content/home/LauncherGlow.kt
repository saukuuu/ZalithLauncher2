package com.movtery.zalithlauncher.ui.screens.content.home

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Subtle layered white halo and crisp rounded outline; no image assets required. */
fun Modifier.launcherGlow(radius: Dp = 14.dp, strength: Float = 0.75f): Modifier =
    this.drawWithContent {
        drawContent()
        val r = radius.toPx()
        val layers = listOf(
            Triple(7.dp.toPx(), 0.055f, 5.dp.toPx()),
            Triple(4.dp.toPx(), 0.12f, 2.dp.toPx()),
            Triple(1.dp.toPx(), 0.82f, 0f)
        )
        layers.forEach { (stroke, opacity, inset) ->
            val w = size.width - inset * 2
            val h = size.height - inset * 2
            if (w > 0f && h > 0f) {
                drawRoundRect(
                    color = Color.White.copy(alpha = (opacity * strength).coerceIn(0f, 1f)),
                    topLeft = Offset(inset, inset),
                    size = Size(w, h),
                    cornerRadius = CornerRadius(r, r),
                    style = Stroke(width = stroke)
                )
            }
        }
    }
