/*
 * Zalith Launcher 2 — download catalog customization.
 * Distributed under GNU GPL v3 or later, like the launcher.
 */
package com.movtery.zalithlauncher.ui.screens.content.download.assets.elements

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

object DownloadCatalogColors {
    val background = Color(0xFF101317)
    val sidebar = Color(0xFF15181D)
    val card = Color(0xFF181C21)
    val field = Color(0xFF13171C)
    val selected = Color(0xFF30363E)
    val text = Color(0xFFF4F5F7)
    val secondary = Color(0xFFB6BEC8)
    val blue = Color(0xFF174D93)
}

private val catalogScheme = darkColorScheme(
    primary = Color(0xFFE5EAF0),
    onPrimary = Color(0xFF15181D),
    primaryContainer = Color(0xFF303B49),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFFCCD5E0),
    onSecondary = Color(0xFF15181D),
    secondaryContainer = Color(0xFF30363E),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF82B5FF),
    background = DownloadCatalogColors.background,
    onBackground = DownloadCatalogColors.text,
    surface = DownloadCatalogColors.card,
    onSurface = DownloadCatalogColors.text,
    surfaceVariant = DownloadCatalogColors.selected,
    onSurfaceVariant = DownloadCatalogColors.secondary,
    outline = Color(0xFF626B76),
    outlineVariant = Color(0xFF343C46)
)

@Composable
fun DownloadCatalogTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = catalogScheme, content = content)
}

/** Static strokes: a fine white edge and a small halo, without animated blur. */
fun Modifier.downloadCatalogGlow(
    radius: Dp = 12.dp,
    strength: Float = 1f
): Modifier = drawWithCache {
    val line = 0.65.dp.toPx()
    val inset = line / 2f
    val bounds = Size((size.width - line).coerceAtLeast(0f), (size.height - line).coerceAtLeast(0f))
    val corner = CornerRadius(radius.toPx(), radius.toPx())
    val opacity = strength.coerceIn(0f, 1f)
    onDrawWithContent {
        drawContent()
        drawRoundRect(Color.White.copy(alpha = 0.045f * opacity), Offset(inset, inset), bounds, corner, style = Stroke(3.dp.toPx()))
        drawRoundRect(Color.White.copy(alpha = 0.085f * opacity), Offset(inset, inset), bounds, corner, style = Stroke(1.6.dp.toPx()))
        drawRoundRect(Color.White.copy(alpha = 0.82f * opacity), Offset(inset, inset), bounds, corner, style = Stroke(line))
    }
}
