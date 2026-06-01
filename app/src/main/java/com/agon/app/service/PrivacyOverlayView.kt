package com.agon.app.service

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.view.View
import android.view.animation.LinearInterpolator
import com.agon.app.data.PrivacySettings
import kotlin.math.max

/**
 * The actual privacy filter renderer.
 *
 * It composites several techniques into one hardware-accelerated draw pass:
 *  1. Dynamic Dimming      — a soft radial darkening biased to the screen center,
 *                            keeping the center brighter than the periphery.
 *  2. Edge Darkening       — strong vignette on the left/right edges where a
 *                            side-viewer's line of sight enters the panel.
 *  3. Directional Contrast — a fine vertical micro-louver stripe pattern that
 *     Reduction               drops contrast at oblique angles (mimics the
 *                            physical privacy-louver film on hardware screens).
 *  4. Adaptive Brightness  — an optional slow shimmer that varies edge density
 *     Masking                 over time to defeat steady off-axis adaptation.
 *
 * The view is non-interactive (FLAG_NOT_TOUCHABLE on the window) so all touches
 * pass through to the apps underneath.
 */
class PrivacyOverlayView(context: Context) : View(context) {

    private var settings: PrivacySettings = PrivacySettings()
    private var phase: Float = 0f

    private val dimPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val edgePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val louverPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private var animator: ValueAnimator? = null

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
    }

    fun update(newSettings: PrivacySettings) {
        settings = newSettings
        configureAdaptive(newSettings.adaptiveMasking && newSettings.enabled)
        invalidate()
    }

    private fun configureAdaptive(on: Boolean) {
        if (on && animator == null) {
            animator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat()).apply {
                duration = 4200L
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    phase = it.animatedValue as Float
                    invalidate()
                }
                start()
            }
        } else if (!on) {
            animator?.cancel()
            animator = null
            phase = 0f
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val s = settings
        if (!s.enabled) return

        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        val tint = s.filterColor.color
        val tintR = (tint.red * 255).toInt()
        val tintG = (tint.green * 255).toInt()
        val tintB = (tint.blue * 255).toInt()

        // Adaptive shimmer factor (subtle, +/- 8% on edge density)
        val shimmer = if (s.adaptiveMasking) 1f + 0.08f * Math.sin(phase.toDouble()).toFloat() else 1f

        // 1. Dynamic Dimming — radial, darker outward, center stays clearer.
        val dimAlpha = (s.dimIntensity * 200f).toInt().coerceIn(0, 230)
        val centerAlpha = (dimAlpha * 0.25f).toInt()
        dimPaint.shader = RadialGradient(
            w / 2f, h / 2f, max(w, h) * 0.72f,
            Color.argb(centerAlpha, tintR, tintG, tintB),
            Color.argb(dimAlpha, tintR, tintG, tintB),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, w, h, dimPaint)

        // 2 & 3 combined band: strong on edges, transparent in the central viewing cone.
        val edgeAlpha = (s.filterDensity * 235f * shimmer).toInt().coerceIn(0, 250)
        val edgeWidthFrac = 0.30f + 0.18f * s.filterDensity
        edgePaint.shader = LinearGradient(
            0f, 0f, w, 0f,
            intArrayOf(
                Color.argb(edgeAlpha, tintR, tintG, tintB),
                Color.argb((edgeAlpha * 0.10f).toInt(), tintR, tintG, tintB),
                Color.argb(0, tintR, tintG, tintB),
                Color.argb((edgeAlpha * 0.10f).toInt(), tintR, tintG, tintB),
                Color.argb(edgeAlpha, tintR, tintG, tintB),
            ),
            floatArrayOf(0f, edgeWidthFrac, 0.5f, 1f - edgeWidthFrac, 1f),
            Shader.TileMode.CLAMP,
        )
        canvas.drawRect(0f, 0f, w, h, edgePaint)

        // 3. Directional Contrast Reduction — fine vertical micro-louver lines.
        if (s.louverPattern) {
            val lineAlpha = (s.filterDensity * 70f).toInt().coerceIn(0, 90)
            louverPaint.color = Color.argb(lineAlpha, 0, 0, 0)
            louverPaint.strokeWidth = 1.2f
            val gap = 4f
            var x = 0f
            while (x < w) {
                canvas.drawLine(x, 0f, x, h, louverPaint)
                x += gap
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        animator?.cancel()
        animator = null
    }
}
