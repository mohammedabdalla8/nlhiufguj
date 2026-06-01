package com.agon.app.data

import androidx.compose.ui.graphics.Color

/**
 * Protection strength presets. Each level maps to a base dim/density that the
 * advanced settings can fine-tune on top of.
 */
enum class PrivacyLevel(val label: String, val arabic: String, val baseDim: Float, val baseDensity: Float) {
    LIGHT("Light", "خفيف", 0.18f, 0.25f),
    MEDIUM("Medium", "متوسط", 0.34f, 0.5f),
    STRONG("Strong", "قوي", 0.55f, 0.85f);

    companion object {
        fun fromName(name: String?): PrivacyLevel =
            entries.firstOrNull { it.name == name } ?: MEDIUM
    }
}

/**
 * Filter tint options applied to the overlay edges.
 */
enum class FilterColor(val label: String, val color: Color) {
    NEUTRAL("Neutral", Color(0xFF000000)),
    INDIGO("Indigo", Color(0xFF1A2050)),
    TEAL("Teal", Color(0xFF06302B)),
    AMBER("Amber", Color(0xFF3A2400)),
    SLATE("Slate", Color(0xFF14181F));

    companion object {
        fun fromName(name: String?): FilterColor =
            entries.firstOrNull { it.name == name } ?: NEUTRAL
    }
}

/**
 * Immutable snapshot of all user-tunable privacy settings.
 * Consumed by both the UI (ViewModel) and the overlay renderer (Service).
 */
data class PrivacySettings(
    val enabled: Boolean = false,
    val level: PrivacyLevel = PrivacyLevel.MEDIUM,
    // Center dimming intensity 0f..1f (Dynamic Dimming)
    val dimIntensity: Float = 0.34f,
    // Edge/vignette filter density 0f..1f (Edge Darkening + Directional Contrast)
    val filterDensity: Float = 0.5f,
    val filterColor: FilterColor = FilterColor.NEUTRAL,
    // Fine micro-louver pattern toggle (Directional Contrast Reduction)
    val louverPattern: Boolean = true,
    // Adaptive brightness masking — pulses edge density subtly
    val adaptiveMasking: Boolean = false,
    // Auto-start the overlay when the app/device boots into the app
    val autoStart: Boolean = false,
)
