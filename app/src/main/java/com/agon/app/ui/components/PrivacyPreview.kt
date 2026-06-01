package com.agon.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.agon.app.data.PrivacySettings
import kotlin.math.max

/**
 * A WYSIWYG preview that mirrors exactly what the real overlay renderer draws,
 * so the user can tune settings and see the effect before committing.
 */
@Composable
fun PrivacyPreview(settings: PrivacySettings, modifier: Modifier = Modifier) {
    val tint = settings.filterColor.color
    Canvas(modifier = modifier.clip(RoundedCornerShape(20.dp))) {
        val w = size.width
        val h = size.height

        // Dynamic dimming — radial
        val dimAlpha = (settings.dimIntensity * 0.78f).coerceIn(0f, 0.9f)
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(
                    tint.copy(alpha = dimAlpha * 0.25f),
                    tint.copy(alpha = dimAlpha),
                ),
                center = Offset(w / 2f, h / 2f),
                radius = max(w, h) * 0.72f,
            ),
            size = Size(w, h),
        )

        // Edge darkening + directional band
        val edgeAlpha = (settings.filterDensity * 0.95f).coerceIn(0f, 0.97f)
        val edgeFrac = 0.30f + 0.18f * settings.filterDensity
        drawRect(
            brush = Brush.horizontalGradient(
                colorStops = arrayOf(
                    0f to tint.copy(alpha = edgeAlpha),
                    edgeFrac to tint.copy(alpha = edgeAlpha * 0.10f),
                    0.5f to Color.Transparent,
                    (1f - edgeFrac) to tint.copy(alpha = edgeAlpha * 0.10f),
                    1f to tint.copy(alpha = edgeAlpha),
                ),
                startX = 0f,
                endX = w,
            ),
            size = Size(w, h),
        )

        // Micro-louver lines
        if (settings.louverPattern) {
            val lineAlpha = (settings.filterDensity * 0.30f).coerceIn(0f, 0.4f)
            var x = 0f
            val gap = 5f
            while (x < w) {
                drawLine(
                    color = Color.Black.copy(alpha = lineAlpha),
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = 1.4f,
                )
                x += gap
            }
        }
    }
}
